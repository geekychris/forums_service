package com.example.forum.cli.commands;

import com.example.forum.cli.services.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

/**
 * Shell commands for comment operations.
 */
@ShellComponent("comment")
@RequiredArgsConstructor
public class CommentCommands {

    private final CommentService commentService;
    private final ObjectMapper objectMapper;

    @ShellMethod(value = "Create a new comment on a post", key = "comment-create")
    public String createComment(
            @Option(longNames = "post-id", shortNames = 'p', description = "Post ID", required = true) Long postId,
            @Option(longNames = "content", shortNames = 'c', description = "Comment content", required = true) String content) {
        try {
            Object response = commentService.createComment(postId, content);
            return "Comment created successfully:\n" + 
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to create comment: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Reply to an existing comment", key = "comment-reply")
    public String createReply(
            @Option(longNames = "comment-id", shortNames = 'c', description = "Parent comment ID", required = true) Long commentId,
            @Option(longNames = "content", shortNames = 't', description = "Reply content", required = true) String content) {
        try {
            Object response = commentService.createReply(commentId, content);
            return "Reply created successfully:\n" + 
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to create reply: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Get a comment by ID", key = "comment-get")
    public String getComment(
            @Option(longNames = "id", shortNames = 'i', description = "Comment ID", required = true) Long id) {
        try {
            Object response = commentService.getCommentById(id);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to get comment: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Update a comment", key = "comment-update")
    public String updateComment(
            @Option(longNames = "id", shortNames = 'i', description = "Comment ID", required = true) Long id,
            @Option(longNames = "content", shortNames = 'c', description = "New content", required = true) String content) {
        try {
            Object response = commentService.updateComment(id, content);
            return "Comment updated successfully:\n" + 
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to update comment: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Delete a comment", key = "comment-delete")
    public String deleteComment(
            @Option(longNames = "id", shortNames = 'i', description = "Comment ID", required = true) Long id) {
        try {
            Object response = commentService.deleteComment(id);
            return "Comment deleted successfully.";
        } catch (Exception e) {
            return "Failed to delete comment: " + e.getMessage();
        }
    }

    @ShellMethod(value = "List comments for a post", key = "comment-list")
    public String listComments(
            @Option(longNames = "post-id", shortNames = 'p', description = "Post ID", required = true) Long postId,
            @Option(longNames = "page", shortNames = 'n', description = "Page number", defaultValue = "0") int page,
            @Option(longNames = "size", shortNames = 's', description = "Page size", defaultValue = "10") int size) {
        try {
            Object response = commentService.getCommentsByPost(postId, page, size);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to list comments: " + e.getMessage();
        }
    }

    @ShellMethod(value = "List replies to a comment", key = "comment-reply-list")
    public String listReplies(
            @Option(longNames = "comment-id", shortNames = 'c', description = "Comment ID", required = true) Long commentId,
            @Option(longNames = "page", shortNames = 'n', description = "Page number", defaultValue = "0") int page,
            @Option(longNames = "size", shortNames = 's', description = "Page size", defaultValue = "10") int size) {
        try {
            Object response = commentService.getRepliesByComment(commentId, page, size);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to list replies: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Upvote a comment", key = "comment-upvote")
    public String upvoteComment(
            @Option(longNames = "id", shortNames = 'i', description = "Comment ID", required = true) Long id) {
        try {
            Object response = commentService.upvoteComment(id);
            return "Comment upvoted successfully.";
        } catch (Exception e) {
            return "Failed to upvote comment: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Downvote a comment", key = "comment-downvote")
    public String downvoteComment(
            @Option(longNames = "id", shortNames = 'i', description = "Comment ID", required = true) Long id) {
        try {
            Object response = commentService.downvoteComment(id);
            return "Comment downvoted successfully.";
        } catch (Exception e) {
            return "Failed to downvote comment: " + e.getMessage();
        }
    }
}