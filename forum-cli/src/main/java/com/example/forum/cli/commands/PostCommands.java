package com.example.forum.cli.commands;

import com.example.forum.cli.services.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

/**
 * Shell commands for post operations.
 */
@ShellComponent("post")
@RequiredArgsConstructor
public class PostCommands {
    private final PostService postService;
    private final ObjectMapper objectMapper;

    @ShellMethod(value = "Create a new post in a forum", key = "post-create")
    public String createPost(
            @Option(longNames = "forum-id", shortNames = 'f', description = "Forum ID", required = true) String forumId,
            @Option(longNames = "title", shortNames = 't', description = "Post title", required = true) String title,
            @Option(longNames = "content", shortNames = 'c', description = "Post content", required = true) String content) {
        try {
            Object response = postService.createPost(forumId, title, content);
            return "Post created successfully:\n" + 
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to create post: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "List posts in a forum", key = "post-list")
    public String listPosts(
            @Option(longNames = "forum-id", shortNames = 'f', description = "Forum ID", required = true) String forumId,
            @Option(longNames = "page", shortNames = 'p', description = "Page number (0-based)", defaultValue = "0") int page,
            @Option(longNames = "size", shortNames = 's', description = "Page size", defaultValue = "10") int size) {
        try {
            Object response = postService.getPostsByForum(forumId, page, size);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to list posts: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Get post details by ID", key = "post-get")
    public String getPost(
            @Option(longNames = "id", shortNames = 'i', description = "Post ID", required = true) String id) {
        try {
            Object response = postService.getPostById(id);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to get post: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Update a post", key = "update")
    public String updatePost(
            @Option(longNames = "id", shortNames = 'i', description = "Post ID", required = true) String id,
            @Option(longNames = "title", shortNames = 't', description = "New post title") String title,
            @Option(longNames = "content", shortNames = 'c', description = "New post content") String content) {
        try {
            Object response = postService.updatePost(id, title, content);
            return "Post updated successfully:\n" + 
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to update post: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Delete a post", key = "delete")
    public String deletePost(
            @Option(longNames = "id", shortNames = 'i', description = "Post ID", required = true) String id) {
        try {
            Object response = postService.deletePost(id);
            return "Post deleted successfully.";
        } catch (Exception e) {
            return "Failed to delete post: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "List posts by a specific user", key = "user-list")
    public String getUserPosts(
            @Option(longNames = "user-id", shortNames = 'u', description = "User ID", required = true) String userId,
            @Option(longNames = "page", shortNames = 'p', description = "Page number (0-based)", defaultValue = "0") int page,
            @Option(longNames = "size", shortNames = 's', description = "Page size", defaultValue = "10") int size) {
        try {
            Object response = postService.getPostsByUser(userId, page, size);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to list user posts: " + e.getMessage();
        }
    }
}
