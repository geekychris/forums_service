package com.example.forum.cli.commands;

import com.example.forum.cli.services.ForumService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

/**
 * Shell commands for forum operations.
 */
@ShellComponent("forum")
@RequiredArgsConstructor
public class ForumCommands {
    private final ForumService forumService;
    private final ObjectMapper objectMapper;

    @ShellMethod(value = "Create a new forum", key = "forum-create")
    public String createForum(
            @Option(longNames = "name", shortNames = 'n', description = "Forum name", required = true) String name,
            @Option(longNames = "description", shortNames = 'd', description = "Forum description", required = true) String description) {
        try {
            Object response = forumService.createForum(name, description);
            return "Forum created successfully:\n" + 
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to create forum: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "List all root forums", key = "forum-list")
    public String listForums() {
        try {
            Object response = forumService.getRootForums();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to list forums: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Get forum details by ID", key = "forum-get")
    public String getForum(
            @Option(longNames = "id", shortNames = 'i', description = "Forum ID", required = true) Long id) {
        try {
            Object response = forumService.getForumById(id);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to get forum: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Update a forum", key = "forum-update")
    public String updateForum(
            @Option(longNames = "id", shortNames = 'i', description = "Forum ID", required = true) Long id,
            @Option(longNames = "name", shortNames = 'n', description = "New forum name") String name,
            @Option(longNames = "description", shortNames = 'd', description = "New forum description") String description) {
        try {
            Object response = forumService.updateForum(id, name, description);
            return "Forum updated successfully:\n" + 
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to update forum: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Delete a forum", key = "forum-delete")
    public String deleteForum(
            @Option(longNames = "id", shortNames = 'i', description = "Forum ID", required = true) Long id) {
        try {
            Object response = forumService.deleteForum(id);
            return "Forum deleted successfully.";
        } catch (Exception e) {
            return "Failed to delete forum: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Create a new subforum", key = "subforum-create")
    public String createSubforum(
            @Option(longNames = "parent-id", shortNames = 'p', description = "Parent forum ID", required = true) Long parentId,
            @Option(longNames = "name", shortNames = 'n', description = "Subforum name", required = true) String name,
            @Option(longNames = "description", shortNames = 'd', description = "Subforum description", required = true) String description) {
        try {
            Object response = forumService.createSubforum(parentId, name, description);
            return "Subforum created successfully:\n" + 
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to create subforum: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "List all subforums of a parent forum", key = "subforum-list")
    public String listSubforums(
            @Option(longNames = "parent-id", shortNames = 'p', description = "Parent forum ID", required = true) Long parentId) {
        try {
            Object response = forumService.getSubforums(parentId);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to list subforums: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Search forums by name", key = "forum-search")
    public String searchForums(
            @Option(longNames = "query", shortNames = 'q', description = "Search query", required = true) String query) {
        try {
            Object response = forumService.searchForums(query);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to search forums: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Move a forum to a new parent or to root level", key = "forum-move")
    public String moveForum(
            @Option(longNames = "id", shortNames = 'i', description = "Forum ID to move", required = true) Long id,
            @Option(longNames = "parent-id", shortNames = 'p', description = "New parent forum ID (omit for root level)") Long parentId) {
        try {
            Object response = forumService.moveForum(id, parentId);
            return "Forum moved successfully:\n" + 
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return "Failed to move forum: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Grant a user access to a forum", key = "forum-access-grant")
    public String grantAccess(
            @Option(longNames = "forum-id", shortNames = 'f', description = "Forum ID", required = true) Long forumId,
            @Option(longNames = "user-id", shortNames = 'u', description = "User ID", required = true) Long userId,
            @Option(longNames = "access-level", shortNames = 'a', description = "Access level (READ, WRITE, ADMIN)", required = true) String accessLevel) {
        try {
            Object response = forumService.grantForumAccess(forumId, userId, accessLevel);
            return "Access granted successfully.";
        } catch (Exception e) {
            return "Failed to grant access: " + e.getMessage();
        }
    }
    
    @ShellMethod(value = "Revoke a user's access to a forum", key = "forum-access-revoke")
    public String revokeAccess(
            @Option(longNames = "forum-id", shortNames = 'f', description = "Forum ID", required = true) Long forumId,
            @Option(longNames = "user-id", shortNames = 'u', description = "User ID", required = true) Long userId) {
        try {
            Object response = forumService.revokeForumAccess(forumId, userId);
            return "Access revoked successfully.";
        } catch (Exception e) {
            return "Failed to revoke access: " + e.getMessage();
        }
    }
}

