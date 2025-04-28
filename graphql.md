# Forum Application API Guide

This guide provides a comprehensive walkthrough of the forum application API, covering both REST endpoints and GraphQL operations. Follow these examples to understand the complete flow from user registration to content management.

## Table of Contents
- [Authentication (REST)](#authentication-rest)
- [Forum Management (REST)](#forum-management-rest)
- [Post Operations (GraphQL)](#post-operations-graphql)
- [Comment Management (REST)](#comment-management-rest)
- [Content Management](#content-management)
- [Complete Workflow](#complete-workflow)

## Authentication (REST)

### Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123!",
    "displayName": "Test User"
  }'
```

Response:
```json
{
  "id": "1",
  "username": "testuser",
  "email": "test@example.com",
  "displayName": "Test User",
  "role": "ROLE_USER",
  "active": true,
  "createdAt": "2025-04-27T10:25:00Z"
}
```

### Login and Get JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Password123!"
  }'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "id": "1",
  "username": "testuser",
  "email": "test@example.com",
  "displayName": "Test User",
  "role": "ROLE_USER"
}
```

Save the token for future requests:
```bash
TOKEN="Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Get Current User Information

Get information about the currently authenticated user:

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: $TOKEN"
```

Response:
```json
{
  "id": "1",
  "username": "testuser",
  "email": "test@example.com",
  "displayName": "Test User",
  "role": "ROLE_USER",
  "active": true,
  "createdAt": "2025-04-27T10:25:00Z"
}
```

### Reset Password

Reset the password for the currently authenticated user:

```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "oldPassword": "Password123!",
    "newPassword": "NewPassword456!"
  }'
```

Response:
```
Password updated successfully
```

## Forum Management (REST)

### Create a New Forum

```bash
curl -X POST http://localhost:8080/api/forums \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "name": "Technology Discussion",
    "description": "A forum to discuss technology trends and news"
  }'
```

Response:
```json
{
  "id": "1",
  "name": "Technology Discussion",
  "description": "A forum to discuss technology trends and news",
  "createdAt": "2025-04-27T10:30:00Z",
  "updatedAt": null,
  "postCount": 0,
  "canRead": true,
  "canWrite": true,
  "canAdmin": true
}
```

Save the forum ID for creating posts:
```bash
FORUM_ID="1"
```

### Get Forum Details

```bash
curl -X GET http://localhost:8080/api/forums/$FORUM_ID \
  -H "Authorization: $TOKEN"
```

Response (with subforums):
```json
{
  "id": "1",
  "name": "Technology Discussion",
  "description": "A forum to discuss technology trends and news",
  "createdAt": "2025-04-27T10:30:00Z",
  "updatedAt": null,
  "postCount": 5,
  "canRead": true,
  "canWrite": true,
  "canAdmin": true,
  "subForums": [
    {
      "id": "2",
      "name": "Programming Languages",
      "description": "Discuss programming languages",
      "parentForumId": "1",
      "parentForumName": "Technology Discussion",
      "postCount": 3,
      "createdAt": "2025-04-27T10:35:00Z"
    }
  ]
}
```

### Get All Root Forums

Retrieve all top-level (root) forums:

```bash
curl -X GET http://localhost:8080/api/forums \
  -H "Authorization: $TOKEN"
```

### Subforum Operations

#### Create a Subforum

Create a new subforum under a parent forum:

```bash
curl -X POST http://localhost:8080/api/forums/$FORUM_ID/subforums \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "name": "Programming Languages",
    "description": "Discuss programming languages and frameworks"
  }'
```

Response:
```json
{
  "id": "2",
  "name": "Programming Languages",
  "description": "Discuss programming languages and frameworks",
  "parentForumId": "1",
  "parentForumName": "Technology Discussion",
  "createdAt": "2025-04-27T10:35:00Z",
  "postCount": 0,
  "canRead": true,
  "canWrite": true,
  "canAdmin": true
}
```

#### Get Subforums

List all subforums of a parent forum:

```bash
curl -X GET http://localhost:8080/api/forums/$FORUM_ID/subforums \
  -H "Authorization: $TOKEN"
```

### Forum Management

#### Update a Forum

```bash
curl -X PUT http://localhost:8080/api/forums/$FORUM_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "name": "Updated Technology Discussion",
    "description": "Updated forum description"
  }'
```

#### Delete a Forum

```bash
curl -X DELETE http://localhost:8080/api/forums/$FORUM_ID \
  -H "Authorization: $TOKEN"
```

Response:
```
Forum deleted successfully
```

#### Move a Forum

Move a forum to become a subforum of another forum:

```bash
curl -X PUT http://localhost:8080/api/forums/2/move?newParentId=3 \
  -H "Authorization: $TOKEN"
```

Move a forum to root level (no parent):

```bash
curl -X PUT http://localhost:8080/api/forums/2/move \
  -H "Authorization: $TOKEN"
```

### Forum Search and Navigation

#### Search Forums

```bash
curl -X GET "http://localhost:8080/api/forums/search?query=technology" \
  -H "Authorization: $TOKEN"
```

#### Get Accessible Forums

List all forums the current user has access to:

```bash
curl -X GET http://localhost:8080/api/forums/accessible \
  -H "Authorization: $TOKEN"
```

### Forum Access Control

#### Grant Forum Access

Give a user access to a forum:

```bash
curl -X POST http://localhost:8080/api/forums/$FORUM_ID/access \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "userId": 2,
    "accessLevel": "WRITE"
  }'
```

Response:
```
Access granted successfully
```

Available access levels:
* `READ` - User can view the forum and its posts
* `WRITE` - User can view and create/edit posts in the forum
* `ADMIN` - User can view, post, and manage forum settings/permissions

#### Update Forum Access

Modify an existing access permission:

```bash
curl -X PUT http://localhost:8080/api/forums/$FORUM_ID/access \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "userId": 2,
    "accessLevel": "ADMIN"
  }'
```

#### Revoke Forum Access

Remove a user's access to a forum:

```bash
curl -X DELETE http://localhost:8080/api/forums/$FORUM_ID/access/2 \
  -H "Authorization: $TOKEN"
```

Response:
```
Access revoked successfully
```

## Post Operations (GraphQL)

### Setup for GraphQL Operations

For all GraphQL operations, we'll use the following curl structure:

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{"query": "YOUR_GRAPHQL_QUERY_HERE"}'
```

Alternatively, access the GraphiQL interface in your browser at: http://localhost:8080/graphiql

Remember to set the HTTP header in GraphiQL:
```
{
  "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9..."
}
```

### Create a New Post

GraphQL Mutation:
```graphql
mutation {
  createPost(
    title: "Getting Started with GraphQL",
    content: "GraphQL is a query language for APIs...",
    forumId: "1"
  ) {
    id
    title
    content
    createdAt
    updatedAt
    forumId
    forumName
    author {
      id
      username
      displayName
    }
    commentCount
    canEdit
    canDelete
  }
}
```

Response:
```json
{
  "data": {
    "createPost": {
      "id": "1",
      "title": "Getting Started with GraphQL",
      "content": "GraphQL is a query language for APIs...",
      "createdAt": "2025-04-27T10:35:00Z",
      "updatedAt": null,
      "forumId": "1",
      "forumName": "Technology Discussion",
      "author": {
        "id": "1",
        "username": "testuser",
        "displayName": "Test User"
      },
      "commentCount": 0,
      "canEdit": true,
      "canDelete": true
    }
  }
}
```

Save the post ID for future operations:
```bash
POST_ID="1"
```

### Query Posts in a Forum

GraphQL Query:
```graphql
query {
  posts(forumId: "1", page: 0, size: 10) {
    content {
      id
      title
      content
      createdAt
      updatedAt
      forumId
      forumName
      commentCount
      author {
        id
        username
        displayName
      }
      contents {
        id
        filename
        contentUrl
      }
      canEdit
      canDelete
    }
    totalElements
    totalPages
    hasNext
    hasPrevious
    size
    number
  }
}
```

### Get Specific Post Details

GraphQL Query:
```graphql
query {
  post(id: "1") {
    id
    title
    content
    createdAt
    updatedAt
    forumId
    forumName
    commentCoun  }
}
```

Response:
```json
{
  "data": {
    "post": {
      "id": "1",
      "title": "Getting Started with GraphQL",
      "content": "GraphQL is a query language for APIs...",
      "createdAt": "2025-04-27T10:35:00Z",
      "updatedAt": null,
      "forumId": "1",
      "forumName": "Technology Discussion",
      "commentCount": 2,
      "author": {
        "id": "1",
        "username": "testuser",
        "displayName": "Test User",
        "email": "test@example.com"
      },
      "contents": [
        {
          "id": "1",
          "filename": "diagram.png",
          "contentUrl": "http://localhost:8080/api/content/1",
          "createdAt": "2025-04-27T10:40:00Z"
        }
      ],
      "canEdit": true,
      "canDelete": true
    }
  }
}
```

### Update a Post

GraphQL Mutation:
```graphql
mutation {
  updatePost(
    id: "1",
    title: "Updated: Getting Started with GraphQL",
    content: "Updated content about GraphQL..."
  ) {
    id
    title
    content
    updatedAt
    }
```

Response:
```json
{
  "data": {
    "updatePost": {
      "id": "1",
      "title": "Updated: Getting Started with GraphQL",
      "content": "Updated content about GraphQL...",
      "updatedAt": "2025-04-27T11:45:00Z",
      "createdAt": "2025-04-27T10:35:00Z",
      "forumId": "1",
      "forumName": "Technology Discussion",
      "commentCount": 2,
      "author": {
        "username": "testuser"
      },
      "canEdit": true,
      "canDelete": true
    }
  }
}
```

### Delete a Post

GraphQL Mutation:
```graphql
mutation {
  deletePost(id: "1")
}
```

Response:
```json
{
  "data": {
    "deletePost": true
  }
}
```

### Get Posts by User

GraphQL Query:
```graphql
query {
  userPosts(authorId: "1", page: 0, size: 10) {
    content {
      id
      title
      content
      createdAt
      updatedA}
```

Response:
```json
{
  "data": {
    "userPosts": {
      "content": [
        {
          "id": "1",
          "title": "Updated: Getting Started with GraphQL",
          "content": "Updated content about GraphQL...",
          "createdAt": "2025-04-27T10:35:00Z",
          "updatedAt": "2025-04-27T11:45:00Z",
          "forumId": "1",
          "forumName": "Technology Discussion",
          "commentCount": 2,
          "contents": [
            {
              "id": "1",
              "filename": "diagram.png"
            }
          ],
          "canEdit": true,
          "canDelete": true
        },
        {
          "id": "2",
          "title": "Another GraphQL Post",
          "content": "More information about GraphQL...",
          "createdAt": "2025-04-27T12:15:00Z",
          "updatedAt": null,
          "forumId": "1",
          "forumName": "Technology Discussion",
          "commentCount": 0,
          "contents": [],
          "canEdit": true,
          "canDelete": true
        }
      ],
      "totalElements": 2,
      "totalPages": 1,
      "size": 10,
      "number": 0
    }
  }
}
```

### Complex GraphQL Query with Post Contents and Comments

You can combine queries to get more information in a single request:

```graphql
query {
  # Get a specific post with details
  post(id: "1") {
    id
    title
    content
    createdAt
    updatedAt
    forumId
    forumName
    commentCount
    author {
      id
      username
      displayName
    }
    contents {
      id
      filename
      contentUrl
    }
    canEdit
    canDelete
  }
  
  # Get posts from the same forum
  posts(forumId: "1", page: 0, size: 5) {
    content {
      id
      title
      author {
        username
      }
    }
    totalElements
  }
  
  # Get posts from the same user
  userPosts(authorId: "1", page: 0, size: 5) {
    content {
      id
      title
      forumName
    }
    totalElements
  }
}
```

## Comment Management (REST)

Comments provide discussion functionality for posts. The comment system supports hierarchical replies, content attachments, and voting.

### Create a Comment

Create a top-level comment on a post:

```bash
curl -X POST http://localhost:8080/api/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "postId": 1,
    "content": "This is a great post about GraphQL!"
  }'
```

Response:
```json
{
  "id": "1",
  "content": "This is a great post about GraphQL!",
  "createdAt": "2025-04-27T11:05:00Z",
  "updatedAt": null,
  "postId": "1",
  "replyCount": 0,
  "author": {
    "id": "1",
    "username": "testuser",
    "displayName": "Test User"
  },
  "canEdit": true,
  "canDelete": true
}
```

### Create a Reply

Reply to an existing comment:

```bash
curl -X POST http://localhost:8080/api/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "parentCommentId": 1,
    "content": "I agree! GraphQL is very powerful."
  }'
```

Response:
```json
{
  "id": "2",
  "content": "I agree! GraphQL is very powerful.",
  "createdAt": "2025-04-27T11:10:00Z",
  "updatedAt": null,
  "postId": "1",
  "parentCommentId": "1",
  "replyCount": 0,
  "author": {
    "id": "1",
    "username": "testuser",
    "displayName": "Test User"
  },
  "canEdit": true,
  "canDelete": true
}
```

### Get a Comment

Retrieve a specific comment by ID:

```bash
curl -X GET http://localhost:8080/api/comments/1 \
  -H "Authorization: $TOKEN"
```

### Update a Comment

Edit an existing comment:

```bash
curl -X PUT http://localhost:8080/api/comments/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "content": "This is an updated comment about GraphQL!"
  }'
```

### Delete a Comment

Remove a comment:

```bash
curl -X DELETE http://localhost:8080/api/comments/1 \
  -H "Authorization: $TOKEN"
```

Response:
```
Comment deleted successfully
```

### List Comments for a Post

Get all top-level comments for a specific post:

```bash
curl -X GET "http://localhost:8080/api/comments/by-post/1?page=0&size=20" \
  -H "Authorization: $TOKEN"
```

Response:
```json
[
  {
    "id": "1",
    "content": "This is a great post about GraphQL!",
    "createdAt": "2025-04-27T11:05:00Z",
    "postId": "1",
    "replyCount": 1,
    "author": {
      "id": "1",
      "username": "testuser",
      "displayName": "Test User"
    },
    "canEdit": true,
    "canDelete": true
  },
  {
    "id": "3",
    "content": "Another top-level comment",
    "createdAt": "2025-04-27T11:15:00Z",
    "postId": "1",
    "replyCount": 0,
    "author": {
      "id": "2",
      "username": "anotheruser",
      "displayName": "Another User"
    },
    "canEdit": false,
    "canDelete": false
  }
]
```

### Get Replies to a Comment

Retrieve all replies to a specific comment:

```bash
curl -X GET "http://localhost:8080/api/comments/1/replies?page=0&size=20" \
  -H "Authorization: $TOKEN"
```

### Get Comments by User

List all comments made by a specific user:

```bash
curl -X GET "http://localhost:8080/api/comments/by-user/1?page=0&size=20" \
  -H "Authorization: $TOKEN"
```

### Search Comments

Search for comments containing specific text:

```bash
curl -X GET "http://localhost:8080/api/comments/search?query=GraphQL&page=0&size=20" \
  -H "Authorization: $TOKEN"
```

### Comment Voting

#### Upvote a Comment

```bash
curl -X POST http://localhost:8080/api/comments/1/upvote \
  -H "Authorization: $TOKEN"
```

#### Downvote a Comment

```bash
curl -X POST http://localhost:8080/api/comments/1/downvote \
  -H "Authorization: $TOKEN"
```

### Comment Content Management

#### Upload Content to a Comment

```bash
curl -X POST http://localhost:8080/api/comments/1/content \
  -H "Authorization: $TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "contentType=IMAGE" \
  -F "description=Supporting diagram" \
  -F "storeInDb=false"
```

Response:
```json
{
  "id": "1",
  "filename": "image_1234567890.jpg",
  "description": "Supporting diagram",
  "contentType": "IMAGE",
  "contentUrl": "http://localhost:8080/api/content/1",
  "createdAt": "2025-04-27T11:30:00Z"
}
```

#### Get Comment Content

List all content attached to a comment:

```bash
curl -X GET http://localhost:8080/api/comments/1/content \
  -H "Authorization: $TOKEN"
```

#### Delete Comment Content

Remove content from a comment:

```bash
curl -X DELETE http://localhost:8080/api/comments/1/content/1 \
  -H "Authorization: $TOKEN"
```

Response:
```
Content deleted successfully
```

## Content Management

### Upload Content (REST)

```bash
curl -X POST http://localhost:8080/api/content/upload \
  -H "Authorization: $TOKEN" \
  -F "file=@/path/to/your/file.jpg" \
  -F "type=IMAGE" \
  -F "postId=1"
```

Response:
```json
{
  "id": "1",
  "filename": "file_1234567890.jpg",
  "contentType": "image/jpeg",
  "size": 1024000,
  "uploadDate": "2025-04-27T10:40:00Z",
  "url": "/api/content/1"
}
```

### Get Content (REST)

```bash
curl -X GET http://localhost:8080/api/content/1 \
  -H "Authorization: $TOKEN"
```

## Complete Workflow

Here's a step-by-step workflow using both REST API and GraphQL:

1. **Register a user** (REST)
2. **Login and get JWT token** (REST)
3. **Get current user info** (REST)
4. **Create a forum** (REST)
5. **Create a post in the forum** (GraphQL)
6. **Upload content for the post** (REST)
7. **Query posts in the forum** (GraphQL)
8. **Get specific post details** (GraphQL)
9. **Create a comment on the post** (REST)
10. **Reply to the comment** (REST)
11. **Upvote a comment** (REST)
12. **Update the post** (GraphQL)
13. **Get all posts by the user** (GraphQL)
14. **Reset password** (REST)
15. **Delete the post** (GraphQL)

### Example Script

```bash
#!/bin/bash

# 1. Register a user
echo "Registering user..."
USER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123!",
    "displayName": "Test User"
  }')
echo $USER_RESPONSE

# 2. Login
echo "Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Password123!"
  }')
echo $LOGIN_RESPONSE

# Extract token (this requires jq for JSON parsing)
TOKEN="Bearer $(echo $LOGIN_RESPONSE | jq -r '.accessToken')"
echo "Token: $TOKEN"

# 3. Get current user info
echo "Getting current user info..."
curl -s -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: $TOKEN"

# 4. Create a forum
echo "Creating forum..."
FORUM_RESPONSE=$(curl -s -X POST http://localhost:8080/api/forums \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "name": "Technology Discussion",
    "description": "A forum to discuss technology trends and news"
  }')
echo $FORUM_RESPONSE

# Extract forum ID
FORUM_ID=$(echo $FORUM_RESPONSE | jq -r '.id')
echo "Forum ID: $FORUM_ID"

# 5. Create a post with GraphQL
echo "Creating post..."
POST_RESPONSE=$(curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d "{\"query\": \"mutation { createPost(title: \\\"Getting Started with GraphQL\\\", content: \\\"GraphQL is a query language for APIs...\\\", forumId: \\\"${FORUM_ID}\\\") { id title content createdAt } }\"}")
echo $POST_RESPONSE

# Extract post ID
POST_ID=$(echo $POST_RESPONSE | jq -r '.data.createPost.id')
echo "Post ID: $POST_ID"

# 6. Query posts in forum
echo "Querying posts..."
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d "{\"query\": \"query { posts(forumId: \\\"${FORUM_ID}\\\", page: 0, size: 10) { content { id title author { username } } totalElements } }\"}"

# 7. Update post
echo "Updating post..."
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d "{\"query\": \"mutation { updatePost(id: \\\"${POST_ID}\\\", title: \\\"Updated: Getting Started with GraphQL\\\", content: \\\"Updated content...\\\") { id title content } }\"}"

# 8. Reset password
echo "Resetting password..."
curl -s -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "oldPassword": "Password123!",
    "newPassword": "NewPassword456!"
  }'

# 9. Delete post
echo "Deleting post..."
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d "{\"query\": \"mutation { deletePost(id: \\\"${POST_ID}\\\") }\"}"
```

## GraphiQL Tips

When using GraphiQL in the browser, remember these tips:

1. Set the HTTP Authorization header with your JWT token
2. Use variables for dynamic values
3. You can use the documentation explorer to understand the schema

Example with variables:
```graphql
# Define query with variables
query GetPost($postId: ID!) {
  post(id: $postId) {
    id
    title
    content
  }
}

# Define variables in the "Query Variables" section
{
  "postId": "1"
}
```

Remember to set HTTP Headers in GraphiQL:
```json
{
  "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9..."
}
```

