#!/bin/bash

# Base URL for the API
BASE_URL="http://localhost:9090"
GRAPHQL_URL="$BASE_URL/graphql"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color
BLUE='\033[0;34m'

# Function to make GraphQL requests
function graphql_request() {
    local query="$1"
    local token="$2"
    
    local response
    if [ -z "$token" ]; then
        response=$(curl -s -X POST "$GRAPHQL_URL" \
            -H "Content-Type: application/json" \
            -d "{\"query\": \"$query\"}")
    else
        response=$(curl -s -X POST "$GRAPHQL_URL" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $token" \
            -d "{\"query\": \"$query\"}")
    fi

    # Check for errors in the response
    if echo "$response" | jq -e '.errors' >/dev/null; then
        echo -e "${RED}Error in GraphQL request:${NC}"
        echo "$response" | jq '.errors'
        return 1
    fi

    echo "$response"
}

# Function to check if jq is installed
if ! command -v jq &> /dev/null; then
    echo -e "${RED}Error: jq is required but not installed.${NC}"
    echo "Please install jq first:"
    echo "  Homebrew: brew install jq"
    echo "  Apt: sudo apt-get install jq"
    echo "  Yum: sudo yum install jq"
    exit 1
fi

# Check if the server is running
if ! curl -s "$BASE_URL/actuator/health" > /dev/null; then
    echo -e "${RED}Error: Server is not running at $BASE_URL${NC}"
    echo "Please start the server first with: mvn spring-boot:run"
    exit 1
fi

echo -e "${BLUE}Starting sample data creation...${NC}"
# Create users (using REST API as that's the auth endpoint)
echo -e "\n${BLUE}Creating users...${NC}"
USER1_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "alice",
        "password": "password123",
        "email": "alice@example.com",
        "displayName": "Alice Smith"
    }')

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to create user alice${NC}"
    exit 1
fi

USER2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "bob",
        "password": "password123",
        "email": "bob@example.com",
        "displayName": "Bob Johnson"
    }')

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to create user bob${NC}"
    exit 1
fi

echo -e "${GREEN}Created users: alice, bob${NC}"

# Get tokens
echo -e "\n${BLUE}Getting authentication tokens...${NC}"
TOKEN1_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "alice",
        "password": "password123"
    }')
TOKEN1=$(echo $TOKEN1_RESPONSE | jq -r '.token')

if [ -z "$TOKEN1" ]; then
    echo -e "${RED}Failed to get token for alice${NC}"
    exit 1
fi

TOKEN2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "bob",
        "password": "password123"
    }')
TOKEN2=$(echo $TOKEN2_RESPONSE | jq -r '.token')

if [ -z "$TOKEN2" ]; then
    echo -e "${RED}Failed to get token for bob${NC}"
    exit 1
fi

echo -e "${GREEN}Obtained authentication tokens${NC}"
# Create forums
echo -e "\n${BLUE}Creating forums...${NC}"
TECH_FORUM_RESPONSE=$(graphql_request "
mutation {
  createForum(
    name: \"Technology\"
    description: \"Discussion about technology\"
  ) {
    id
    name
  }
}" "$TOKEN1")

TECH_FORUM_ID=$(echo $TECH_FORUM_RESPONSE | jq -r '.data.createForum.id')

if [ -z "$TECH_FORUM_ID" ]; then
    echo -e "${RED}Failed to create Technology forum${NC}"
    exit 1
fi

GAMING_FORUM_RESPONSE=$(graphql_request "
mutation {
  createForum(
    name: \"Gaming\"
    description: \"Gaming discussions\"
  ) {
    id
    name
  }
}" "$TOKEN1")

GAMING_FORUM_ID=$(echo $GAMING_FORUM_RESPONSE | jq -r '.data.createForum.id')

if [ -z "$GAMING_FORUM_ID" ]; then
    echo -e "${RED}Failed to create Gaming forum${NC}"
    exit 1
fi

echo -e "${GREEN}Created forums: Technology (ID: $TECH_FORUM_ID), Gaming (ID: $GAMING_FORUM_ID)${NC}"
# Create posts
echo -e "\n${BLUE}Creating posts...${NC}"
TECH_POST_RESPONSE=$(graphql_request "
mutation {
  createPost(
    title: \"The Future of AI\"
    content: \"AI is transforming how we work and live...\"
    forumId: \"$TECH_FORUM_ID\"
  ) {
    id
    title
  }
}" "$TOKEN1")

TECH_POST_ID=$(echo $TECH_POST_RESPONSE | jq -r '.data.createPost.id')

if [ -z "$TECH_POST_ID" ]; then
    echo -e "${RED}Failed to create AI post${NC}"
    exit 1
fi

GAMING_POST_RESPONSE=$(graphql_request "
mutation {
  createPost(
    title: \"Best Games of 2025\"
    content: \"Here are my top picks for 2025...\"
    forumId: \"$GAMING_FORUM_ID\"
  ) {
    id
    title
  }
}" "$TOKEN2")

GAMING_POST_ID=$(echo $GAMING_POST_RESPONSE | jq -r '.data.createPost.id')

if [ -z "$GAMING_POST_ID" ]; then
    echo -e "${RED}Failed to create Games post${NC}"
    exit 1
fi

echo -e "${GREEN}Created posts: AI (ID: $TECH_POST_ID), Games (ID: $GAMING_POST_ID)${NC}"
# Create comments
echo -e "\n${BLUE}Creating comments...${NC}"
COMMENT1_RESPONSE=$(graphql_request "
mutation {
  addComment(
    postId: \"$TECH_POST_ID\"
    content: \"Great insights about AI!\"
  ) {
    id
    content
  }
}" "$TOKEN2")

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to create comment on AI post${NC}"
    exit 1
fi

COMMENT2_RESPONSE=$(graphql_request "
mutation {
  addComment(
    postId: \"$GAMING_POST_ID\"
    content: \"Don't forget about Indie games!\"
  ) {
    id
    content
  }
}" "$TOKEN1")

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to create comment on Games post${NC}"
    exit 1
fi

echo -e "${GREEN}Created comments on both posts${NC}"
# Verify creation by fetching posts with comments
echo -e "\n${BLUE}Verifying created content...${NC}"
VERIFY_RESPONSE=$(graphql_request "
query {
  posts(forumId: \"$TECH_FORUM_ID\", page: 0, size: 10) {
    content {
      id
      title
      author {
        username
      }
      comments {
        content
        author {
          username
        }
      }
    }
  }
}" "$TOKEN1")

echo -e "\n${GREEN}Created content summary:${NC}"
echo "$VERIFY_RESPONSE" | jq -r '.data.posts.content[] | "Post: \(.title) by \(.author.username)\nComments: \(.comments | map(.content) | join(", "))\n"'

echo -e "\n${GREEN}Sample data creation completed successfully!${NC}"
