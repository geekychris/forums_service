# Forum Application

A Spring Boot application for managing forums, posts, and comments with nested structures and content attachments.

## Project Overview

This Forum Application is a comprehensive platform allowing users to create and participate in discussion forums. It features a hierarchical structure where forums can contain subforums, posts can have comments, and comments can have replies. Both posts and comments can include various types of content attachments such as images, documents, videos, and audio files.

### Architecture

The application follows a standard Spring Boot architecture with the following components:

- **Controllers**: RESTful API endpoints for client interaction
- **Services**: Business logic implementation
- **Repositories**: Data access layer using Spring Data JPA
- **Models**: Entity classes representing database tables
- **DTOs**: Data Transfer Objects for API request/response
- **Exception Handling**: Custom exceptions with appropriate HTTP status codes

### Key Features

- **Hierarchical Forum Structure**: Forums can contain subforums to any depth
- **User Access Control**: Granular permissions (READ, WRITE, ADMIN) for forums
- **Rich Content Support**: Posts and comments can include various media types
- **Nested Comments**: Comments can have replies to any depth
|- **Flexible Storage**: Content can be stored in the database or on disk
|- **JWT Authentication**: Secure API access with token-based authentication
|- **RESTful API**: Well-documented API for easy integration
|- **Dual API Support**: Both RESTful and GraphQL APIs available

## Setup Instructions

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- PostgreSQL 13 or higher (for production)
- H2 Database (used for development/testing)

### Configuration

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/forum.git
   cd forum
   ```

2. Configure application properties in `src/main/resources/application.properties`:
   - Database connection
   - JWT secret
   - File storage path

3. Build the application:
   ```bash
   mvn clean package
   ```

### Running the Application

#### Development Mode

```bash
mvn spring-boot:run
```

The application will start on port 8080 by default with the H2 in-memory database.

#### Production Mode

1. Set up a PostgreSQL database
2. Update application.properties with PostgreSQL connection details
3. Run the application:
   ```bash
   java -jar target/forum-0.0.1-SNAPSHOT.jar
   ```

## Database Schema

The application uses the following main entities:

- **User**: Represents system users
- **Forum**: Represents discussion forums, with self-referential relationship for nesting
- **Post**: Represents forum posts created by users
- **Comment**: Represents comments on posts, with self-referential relationship for replies
- **Content**: Represents media content attached to posts or comments
- **ForumAccess**: Represents user access levels to forums

### Entity Relationships

- A Forum can have multiple subforums (parent-child relationship)
- A Forum contains multiple Posts
- A Post belongs to exactly one Forum
- A Post has multiple Comments
- A Comment belongs to exactly one Post
- A Comment can have multiple replies (parent-child relationship)
- Posts and Comments can have multiple Content attachments
- Users can have different access levels to different Forums

## API Documentation

The API follows RESTful principles with JSON payloads. All endpoints (except authentication and public endpoints) require JWT authentication.

### Authentication

The application uses JWT (JSON Web Token) for authentication. All protected endpoints require a valid JWT token in the Authorization header.

#### Authentication Endpoints

- **POST /api/auth/register**: Register a new user
- **POST /api/auth/login**: Authenticate and receive JWT token
- **POST /api/auth/reset-password**: Reset user password
- **GET /api/auth/me**: Get current user information

#### Registration Requirements

When registering a new user (`/api/auth/register`), the following fields are required:

| Field        | Requirements                                                                   |
|--------------|--------------------------------------------------------------------------------|
| username     | 3-50 characters, can contain letters, numbers, dots, underscores, and hyphens  |
| password     | Minimum 8 characters                                                           |
| email        | Valid email format                                                             |
| displayName  | 2-100 characters                                                               |

#### Common Authentication Errors

- **401 Unauthorized**: Invalid credentials or missing/expired JWT token
- **403 Forbidden**: Valid authentication but insufficient permissions
- **405 Method Not Allowed**: Using wrong HTTP method (e.g., GET instead of POST)

### Forums

- **GET /api/forums**: Get all root forums
- **GET /api/forums/{id}**: Get forum by ID
- **POST /api/forums**: Create a new forum
- **PUT /api/forums/{id}**: Update a forum
- **DELETE /api/forums/{id}**: Delete a forum
- **GET /api/forums/{parentId}/subforums**: Get subforums
- **POST /api/forums/{parentId}/subforums**: Create a subforum
- **PUT /api/forums/{id}/move**: Move a forum to a new parent
- **GET /api/forums/search**: Search for forums
- **GET /api/forums/accessible**: Get accessible forums for current user
- **POST /api/forums/{id}/access**: Grant a user access to a forum
- **PUT /api/forums/{id}/access**: Update a user's forum access
- **DELETE /api/forums/{id}/access/{userId}**: Revoke a user's forum access

### Posts

- **GET /api/posts/{id}**: Get post by ID
- **POST /api/posts**: Create a new post
- **PUT /api/posts/{id}**: Update a post
- **DELETE /api/posts/{id}**: Delete a post
- **GET /api/posts/by-forum/{forumId}**: Get posts in a forum
- **GET /api/posts/by-user/{userId}**: Get posts by a user
- **GET /api/posts/search**: Search for posts
- **POST /api/posts/{id}/content**: Upload content to a post
- **GET /api/posts/{id}/content**: Get all content for a post
- **DELETE /api/posts/{postId}/content/{contentId}**: Delete content from a post

### Comments

- **GET /api/comments/{id}**: Get comment by ID
- **POST /api/comments**: Create a comment (on post or as reply to another comment)
- **PUT /api/comments/{id}**: Update a comment
- **DELETE /api/comments/{id}**: Delete a comment
- **GET /api/comments/by-post/{postId}**: Get comments for a post
- **GET /api/comments/{commentId}/replies**: Get replies to a comment
- **GET /api/comments/by-user/{userId}**: Get comments by a user
- **POST /api/comments/{id}/content**: Upload content to a comment
- **GET /api/comments/{id}/content**: Get all content for a comment
- **DELETE /api/comments/{commentId}/content/{contentId}**: Delete content

### Example Requests

#### Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123",
    "email": "john@example.com",
    "displayName": "John Doe"
  }'
```

**Successful Response (201 Created):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "displayName": "John Doe",
  "role": "USER",
  "active": true,
  "createdAt": "2025-04-26T23:36:20.772466"
}
```

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

**Successful Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["USER"]
}
```

This will return a JWT token to use in subsequent requests. The token must be included in the Authorization header for all protected endpoints.

#### Using JWT Token in Requests

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### Reset Password

```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "currentPassword": "password123",
    "newPassword": "newSecurePassword456"
  }'
```

#### Authentication Troubleshooting

##### Common Issues and Solutions

1. **"Method Not Allowed" Error (405)**
   - **Problem**: You're using the wrong HTTP method when calling an endpoint.
   - **Solution**: Registration and login endpoints require POST methods, not GET. Use `-X POST` in your curl commands or ensure your API client is using the correct HTTP method.
   - **Example Fix**:
     ```bash
     # Incorrect
     curl http://localhost:8080/api/auth/register
     
     # Correct
     curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{...}'
     ```

2. **Missing Required Fields (400 Bad Request)**
   - **Problem**: Your registration request is missing required fields.
   - **Solution**: Ensure all required fields (username, password, email, displayName) are included in your request body.
   - **Example**: See registration request example above for proper format.

3. **Invalid Token (401 Unauthorized)**
   - **Problem**: JWT token is invalid, expired, or malformed.
   - **Solution**: 
     - Ensure you're using the exact token returned by the login endpoint
     - Check if the token has expired (default expiration is 24 hours)
     - Re-authenticate to get a fresh token
   - **Example**: 
     ```bash
     # Refresh your token
     curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"johndoe","password":"password123"}'
     ```

4. **Content-Type Header Missing (415 Unsupported Media Type)**
   - **Problem**: Requests are sent without specifying the Content-Type header.
   - **Solution**: Always include `-H "Content-Type: application/json"` in your requests when sending JSON data.

5. **JWT Token Format (401 Unauthorized)**
   - **Problem**: Incorrect format when using JWT token in Authorization header.
   - **Solution**: Ensure you prefix the token with "Bearer " (including the space).
   - **Example**:
     ```bash
     # Incorrect
     curl -H "Authorization: eyJhbGciOiJIUzI1NiJ9..." http://localhost:8080/api/auth/me
     
     # Correct
     curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." http://localhost:8080/api/auth/me
     ```

#### Create a Forum

```bash
curl -X POST http://localhost:8080/api/forums \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Technology",
    "description": "Discussions about technology"
  }'
```

#### Create a Post

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Hello World",
    "content": "This is my first post!",
    "forumId": 1
  }'
```
```

## GraphQL API

The application provides a GraphQL API alongside the REST API, offering more flexible data fetching capabilities.

### GraphQL Endpoints

- **GraphQL API**: `http://localhost:8080/graphql`
- **GraphiQL Interface**: `http://localhost:8080/graphiql` (Development only)

### Example GraphQL Queries

#### Fetch Posts with Author and Comments

```graphql
query {
  posts(forumId: "1", page: 0, size: 10) {
    content {
      id
      title
      content
      author {
        username
        email
      }
      comments {
        id
        content
        author {
          username
        }
      }
    }
    totalElements
    totalPages
    hasNext
    hasPrevious
  }
}
```

#### Get Single Post

```graphql
query {
  post(id: "1") {
    id
    title
    content
    author {
      username
    }
    createdAt
    updatedAt
  }
}
```

#### Create New Post

```graphql
mutation {
  createPost(
    title: "Hello GraphQL"
    content: "This is my first GraphQL post!"
    forumId: "1"
  ) {
    id
    title
    content
    author {
      username
    }
  }
}
```

#### Update Post

```graphql
mutation {
  updatePost(
    id: "1"
    title: "Updated Title"
    content: "Updated content"
  ) {
    id
    title
    content
    updatedAt
  }
}
```

#### Delete Post

```graphql
mutation {
  deletePost(id: "1")
}
```

### API Comparison

#### REST vs GraphQL

1. **REST API**
   - Traditional endpoint-based approach
   - Fixed response structure
   - Multiple endpoints for different data needs
   - Suitable for simple CRUD operations

2. **GraphQL API**
   - Single endpoint (`/graphql`)
   - Flexible response structure
   - Client specifies exactly what data they need
   - Reduced over-fetching and under-fetching
   - Built-in introspection and documentation

### Authentication

Both REST and GraphQL APIs use the same JWT authentication mechanism. For GraphQL:

1. Obtain JWT token through REST login endpoint
2. Include token in GraphQL requests:

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "query": "query { posts(forumId: \"1\") { content { id title } } }"
  }'
```

### GraphQL Development Tools

1. **GraphiQL Interface**
   - Available at `http://localhost:8080/graphiql`
   - Interactive query editor
   - Schema documentation
   - Query auto-completion
   - Request/response testing

2. **Schema Exploration**
   - Use GraphiQL's "Docs" panel to explore available types and operations
    - Schema is self-documenting

## Sample Data Generation

A script is provided to populate the application with sample data using both REST and GraphQL APIs. The script creates:
- Two users (alice and bob)
- Two forums (Technology and Gaming)
- Posts in each forum
- Comments on the posts

### Prerequisites

- jq (JSON processor) must be installed:
  - macOS: `brew install jq`
  - Linux: `sudo apt-get install jq` or `sudo yum install jq`
- The application must be running locally on port 8080

### Running the Script

```bash
# Make the script executable (if not already)
chmod +x scripts/populate_graphql.sh

# Run the script
./scripts/populate_graphql.sh
```

The script provides colored output showing the progress of data creation and will verify the created content at the end.

### Using the Sample Data

After running the script, you can:
1. Log in as either user:
   - Username: alice, Password: password123
   - Username: bob, Password: password123
2. Use the GraphiQL interface (http://localhost:8080/graphiql) to explore the created data
3. Use the REST API endpoints with the created forum and post IDs

Example GraphQL query to explore the created data:
```graphql
query {
  posts(forumId: "1", page: 0, size: 10) {
    content {
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
    totalElements
    totalPages
  }
}
```

## Testing Instructions
### Unit Tests

Run unit tests with:

```bash
mvn test
```

### Integration Tests

Run integration tests with:

```bash
mvn verify
```

### Manual Testing

1. Start the application in development mode
2. Use the provided Swagger UI at `http://localhost:8080/swagger-ui.html`
3. Use the example curl commands above to test API endpoints
4. Use the H2 console at `http://localhost:8080/h2-console` to examine the database (dev mode only)

## Security Considerations

### Authentication

- The application uses JWT tokens for authentication
- Tokens expire after 24 hours by default (configurable)
- Passwords are hashed using BCrypt before storage

### Authorization

- Forum access is controlled through ForumAccess entities with three levels:
  - READ: Can view forums and posts
  - WRITE: Can create posts and comments
  - ADMIN: Can manage forum settings and user access

### Content Security

- Uploaded files are validated for:
  - File type (restricted to allowed extensions)
  - File size (configurable maximum)
- Content paths are sanitized to prevent path traversal attacks

### CSRF Protection

- CSRF protection is disabled for the API since it uses JWT authentication
- For web applications consuming this API, implement CSRF protection at the client level

## File Storage Configuration

### Storage Options

Content attachments can be stored in two ways:
1. **Database Storage**: Files are stored as binary data in the database
2. **File System Storage**: Files are stored on disk with references in the database

### Configuration

File storage is configured in `application.properties`:

```properties
# Storage path for file system storage
app.content.storage.path=./content-storage

# Maximum file size
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB
```

### Decision Factors

Choose database storage for:
- Small files
- Simple deployment (no separate file system management)
- Atomic transactions with database records

Choose file system storage for:
- Larger files
- Better performance for streaming content
- Lower database load

## License

This project is licensed under the MIT License - see the LICENSE file for details.

•  GraphiQL interface: http://localhost:8080/graphiql