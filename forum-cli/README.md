# Forum CLI

A command-line interface client for interacting with the Forum application. This CLI tool provides a convenient way to manage forums, posts, comments, and content without using a web browser.

## Table of Contents

- [Installation](#installation)
- [Authentication](#authentication)
- [Available Commands](#available-commands)
  - [Authentication Commands](#authentication-commands)
  - [Forum Commands](#forum-commands)
  - [Post Commands](#post-commands)
  - [Comment Commands](#comment-commands)
  - [Content Commands](#content-commands)
- [Common Usage Patterns](#common-usage-patterns)
- [Error Handling](#error-handling)

## Installation

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Building

1. Clone the repository
2. Build the CLI tool:

```bash
cd forum
mvn clean package -am -pl forum-cli
```

### Running

```bash
java -jar forum-cli/target/forum-cli-0.0.1-SNAPSHOT.jar
```

You can also create an alias for ease of use:

```bash
alias forum-cli='java -jar /path/to/forum-cli/target/forum-cli-0.0.1-SNAPSHOT.jar'
```

## Authentication

The CLI tool uses JWT tokens for authentication. All operations that require authentication will automatically use the stored token. If no token is available, you must login first.

### Register a New User

```bash
register --username myuser --email user@example.com --password mypassword --display-name "My User"
```

### Login

```bash
login --username myuser --password mypassword
```

After login, the token is stored in `~/.forum-cli/token` and will be used for subsequent commands.

### Check Current User

```bash
whoami
```

### Logout

```bash
logout
```

## Available Commands

### Authentication Commands

| Command | Description | Example |
|---------|-------------|---------|
| `login` | Login to the forum API | `login -u myuser -p mypassword` |
| `register` | Register a new user account | `register -u newuser -e user@example.com -p password -d "Display Name"` |
| `logout` | Logout and remove stored token | `logout` |
| `whoami` | Display current user information | `whoami` |

### Forum Commands

| Command | Description | Example |
|---------|-------------|---------|
| `forum-create` | Create a new forum | `forum-create -n "Technology" -d "Tech discussions"` |
| `forum-list` | List all root forums | `forum-list` |
| `forum-get` | Get forum details by ID | `forum-get -i 1` |
| `forum-update` | Update a forum | `forum-update -i 1 -n "New Name" -d "New description"` |
| `forum-delete` | Delete a forum | `forum-delete -i 1` |
| `subforum-create` | Create a new subforum | `subforum-create -p 1 -n "Subcategory" -d "Description"` |
| `subforum-list` | List all subforums of a parent forum | `subforum-list -p 1` |
| `forum-search` | Search forums by name | `forum-search -q technology` |
| `forum-move` | Move a forum to a new parent or to root level | `forum-move -i 2 -p 1` |
| `forum-access-grant` | Grant a user access to a forum | `forum-access-grant -f 1 -u 2 -a WRITE` |
| `forum-access-revoke` | Revoke a user's access to a forum | `forum-access-revoke -f 1 -u 2` |

### Post Commands

| Command | Description | Example |
|---------|-------------|---------|
| `post-create` | Create a new post in a forum | `post-create -f 1 -t "Post Title" -c "Post content"` |
| `post-list` | List posts in a forum | `post-list -f 1 -p 0 -s 10` |
| `post-get` | Get post details by ID | `post-get -i 1` |
| `post-update` | Update a post | `post-update -i 1 -t "New Title" -c "Updated content"` |
| `post-delete` | Delete a post | `post-delete -i 1` |
| `user-posts` | List posts by a specific user | `user-posts -u 1 -p 0 -s 10` |

### Comment Commands

| Command | Description | Example |
|---------|-------------|---------|
| `comment-create` | Create a comment on a post | `comment-create -p 1 -c "Great post!"` |
| `comment-reply` | Reply to an existing comment | `comment-reply -p 1 -c "I agree!"` |
| `comment-get` | Get a comment by ID | `comment-get -i 1` |
| `comment-update` | Update a comment | `comment-update -i 1 -c "Updated comment"` |
| `comment-delete` | Delete a comment | `comment-delete -i 1` |
| `post-comments` | List comments for a post | `post-comments -p 1 -a 0 -s 20` |
| `comment-replies` | List replies to a comment | `comment-replies -c 1 -p 0 -s 20` |
| `comment-upvote` | Upvote a comment | `comment-upvote -i 1` |
| `comment-downvote` | Downvote a comment | `comment-downvote -i 1` |

### Content Commands

| Command | Description | Example |
|---------|-------------|---------|
| `post-upload` | Upload content to a post | `post-upload -p 1 -f ./image.jpg -t IMAGE -d "My diagram" -s false` |
| `comment-upload` | Upload content to a comment | `comment-upload -c 1 -f ./image.jpg -t IMAGE -d "Screenshot" -s false` |
| `post-content-list` | List content for a post | `post-content-list -p 1` |
| `comment-content-list` | List content for a comment | `comment-content-list -c 1` |
| `content-delete` | Delete content | `content-delete -i 1` |

## Common Usage Patterns

### Complete Forum and Post Workflow

```bash
# Login first
login -u myuser -p mypassword

# Create a new forum
forum-create -n "Programming" -d "Programming discussions"
# Note the forum ID (e.g., 1)

# Create a post in the forum
post-create -f 1 -t "Learning Java" -c "What resources do you recommend for learning Java?"
# Note the post ID (e.g., 1)

# Add a comment to the post
comment-create -p 1 -c "I recommend the official documentation and practice projects"

# Upload an image to the post
post-upload -p 1 -f ./java-diagram.png -t IMAGE -d "Java learning path" -s false

# List all comments on the post
post-comments -p 1

# List all content attached to the post
post-content-list -p 1

# Update the post
post-update -i 1 -t "Learning Java - Resources" -c "What resources do you recommend for learning Java? Books, tutorials, etc."
```

### Forum Management

```bash
# Create main categories
forum-create -n "Programming" -d "Programming discussions"
forum-create -n "Design" -d "Design discussions"

# Create subforums under Programming
subforum-create -p 1 -n "Java" -d "Java discussions"
subforum-create -p 1 -n "Python" -d "Python discussions"

# List all top-level forums
forum-list

# List all subforums under Programming
subforum-list -p 1

# Move a subforum to another parent
forum-move -i 3 -p 2  # Move Java to be under Design

# Grant a user admin access to a forum
forum-access-grant -f 1 -u 2 -a ADMIN
```

## Error Handling

The CLI tool handles errors in the following ways:

1. **Connection errors**: If the server is unavailable, error messages will indicate the connection problem.
2. **Authentication errors**: If an operation requires authentication and you are not logged in, you'll be prompted to login.
3. **Permission errors**: If you don't have sufficient permissions for an operation, error messages will indicate the permission issue.
4. **Input validation**: The CLI validates inputs and provides helpful error messages for invalid inputs.

### Common Error Resolution

- **Authentication failures**: Run `login` to reauthenticate.
- **"Not found" errors**: Verify that the ID you're using exists.
- **Permission errors**: Make sure you have the right access level (READ, WRITE, ADMIN) for the operation.
- **File upload errors**: Verify the file exists and the path is correct.

### Debug Mode

For more detailed error information, run the CLI with the debug flag:

```bash
java -jar forum-cli/target/forum-cli-0.0.1-SNAPSHOT.jar --logging.level.com.example.forum.cli=DEBUG
```

