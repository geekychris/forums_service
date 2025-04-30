# Forum Management System - Swing Client

A Java Swing desktop application for managing forums, posts, and comments. This client connects to a REST API backend for data management.

## Features

- Interactive forum management
    - Create, edit, and delete forums
    - Hierarchical forum structure
    - Context menus for forum operations
    - Forum tree navigation

- Post management
    - Create and edit posts
    - View post content
    - Delete posts
    - Comment management
    - Post content attachments

- User management
    - User authentication
    - User registration
    - Profile management
    - Role-based access control

- Modern UI
    - Split-pane interface
    - Tree-based forum navigation
    - Post list with sorting
    - Content preview
    - Context menus
    - Dialog-based interactions

## Requirements

- Java 21 or later
- Maven for building
- REST API backend running on http://localhost:9090

## Quick Start

1. Ensure prerequisites are installed (Java 21, Maven, backend API)

2. Clone the repository:
   ```bash
   git clone [repository-url]
   cd forum
   ```

3. Build and run:
   ```bash
   mvn clean package
   java -jar target/forum-client-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

4. When the application starts:
    - A login dialog will appear
    - Register a new account or log in
    - Start managing forums and posts

## Tips
- Use right-click menus for most operations
- Double-click posts to view content
- The refresh button updates current view
- Check the status bar for login info

## Configuration

The application connects to a REST API at http://localhost:9090. The API endpoint can be configured by updating the BASE_URL constant in ApiClient.java.

## Project Structure

- `src/main/java/com/forum/app/`
    - `ForumApp.java` - Main application entry point
    - `MainFrame.java` - Main application window
    - `ForumTreePanel.java` - Forum navigation panel
    - `PostListPanel.java` - Post management panel
    - `LoginPanel.java` - Authentication dialog
    - `ApiClient.java` - REST API client
    - `DialogFactory.java` - Common dialog creation
    - Data Transfer Objects:
        - `ForumDto.java` - Forum data
        - `PostDto.java` - Post data
        - `CommentDto.java` - Comment data
        - `UserDto.java` - User data
        - `PostContentDto.java` - Post attachments
        - `CommentContentDto.java` - Comment attachments
        - `ApiResponse.java` - API response wrapper

## Client-Server Communication

The client communicates with the server using HTTP requests:
- JWT authentication for secure access
- JSON request/response format
- RESTful API endpoints
- Error handling with user feedback

For backend API documentation, refer to the server documentation.

## Development

### Prerequisites

1. JDK 21 or later
2. Maven 3.8 or later
3. Your favorite Java IDE (Eclipse, IntelliJ IDEA, VS Code, etc.)
4. Git for version control

### Setup

1. Clone the repository:
   ```bash
   git clone [repository-url]
   cd forum
   ```

2. Import as Maven project in your IDE
    - Eclipse: Import > Maven > Existing Maven Projects
    - IntelliJ IDEA: Import Project > Maven
    - VS Code: Install Java Extension Pack, open folder

3. Build the project:
   ```bash
   mvn clean install
   ```

### Making Changes

1. Create a new branch for your feature/fix:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes
3. Test thoroughly
4. Create a pull request

### Code Style

- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Keep methods focused and concise
- Use meaningful variable names
- Handle errors appropriately

For more details about the backend API, refer to the server documentation.

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to:
- Report bugs
- Suggest features
- Submit pull requests
- Follow coding guidelines

For bug reports and feature requests, please use the GitHub issue templates provided.

Changes to the project are documented in the [CHANGELOG.md](CHANGELOG.md) file.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
