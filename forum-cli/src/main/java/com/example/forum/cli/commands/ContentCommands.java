package com.example.forum.cli.commands;

import com.example.forum.cli.services.ContentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.ShellComponent;
import java.io.File;

/**
 * Commands for content management (uploading files, listing attachments, etc.)
 */
@ShellComponent
@Command(group = "Content")
@RequiredArgsConstructor
public class ContentCommands {

    private final ContentService contentService;
    private final ObjectMapper objectMapper;

    @Command(command = "post-upload", description = "Upload content to a post")
    public String uploadToPost(
            @Option(longNames = "post-id", shortNames = 'p', description = "Post ID", required = true) Long postId,
            @Option(longNames = "file", shortNames = 'f', description = "File path", required = true) String filePath,
            @Option(longNames = "type", shortNames = 't', description = "Content type (IMAGE, VIDEO, DOCUMENT, etc.)", defaultValue = "IMAGE") String contentType,
            @Option(longNames = "description", shortNames = 'd', description = "Content description") String description,
            @Option(longNames = "store-in-db", shortNames = 's', description = "Store in database", defaultValue = "false") boolean storeInDb) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                return "File not found: " + filePath;
            }
            
            Object response = contentService.uploadPostContent(postId, file, contentType, description, storeInDb);
            return "Content uploaded successfully:\n" + 
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to upload content: " + e.getMessage();
        }
    }
    
    @Command(command = "comment-upload", description = "Upload content to a comment")
    public String uploadToComment(
            @Option(longNames = "comment-id", shortNames = 'c', description = "Comment ID", required = true) Long commentId,
            @Option(longNames = "file", shortNames = 'f', description = "File path", required = true) String filePath,
            @Option(longNames = "type", shortNames = 't', description = "Content type (IMAGE, VIDEO, DOCUMENT, etc.)", defaultValue = "IMAGE") String contentType,
            @Option(longNames = "description", shortNames = 'd', description = "Content description") String description,
            @Option(longNames = "store-in-db", shortNames = 's', description = "Store in database", defaultValue = "false") boolean storeInDb) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                return "File not found: " + filePath;
            }
            
            Object response = contentService.uploadCommentContent(commentId, file, contentType, description, storeInDb);
            return "Content uploaded successfully:\n" + 
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to upload content: " + e.getMessage();
        }
    }
    
    @Command(command = "post-content-list", description = "List content for a post")
    public String getPostContent(
            @Option(longNames = "post-id", shortNames = 'p', description = "Post ID", required = true) Long postId) {
        try {
            Object response = contentService.getPostContent(postId);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to list post content: " + e.getMessage();
        }
    }
    
    @Command(command = "comment-content-list", description = "List content for a comment")
    public String getCommentContent(
            @Option(longNames = "comment-id", shortNames = 'c', description = "Comment ID", required = true) Long commentId) {
        try {
            Object response = contentService.getCommentContent(commentId);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to list comment content: " + e.getMessage();
        }
    }
    
    @Command(command = "content-delete", description = "Delete content")
    public String deleteContent(
            @Option(longNames = "content-id", shortNames = 'i', description = "Content ID", required = true) Long contentId) {
        try {
            Object response = contentService.deleteContent(contentId);
            return "Content deleted successfully.";
        } catch (Exception e) {
            return "Failed to delete content: " + e.getMessage();
        }
    }
}
