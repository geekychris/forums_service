package com.example.forum.controller;

import com.example.forum.dto.forum.CreateForumRequest;
import com.example.forum.dto.forum.ForumAccessRequest;
import com.example.forum.dto.forum.ForumResponse;
import com.example.forum.dto.forum.UpdateForumRequest;
import com.example.forum.model.AccessLevel;
import com.example.forum.model.Forum;
import com.example.forum.model.User;
import com.example.forum.service.ForumService;
import com.example.forum.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing forums.
 */
@RestController
@RequestMapping("/api/forums")
@RequiredArgsConstructor
@Tag(name = "Forums", description = "Forum management API")
public class ForumController {

    private final ForumService forumService;
    private final UserService userService;

    /**
     * Create a new forum.
     *
     * @param createForumRequest the forum creation request
     * @return the created forum
     */
    @PostMapping
    @Operation(summary = "Create a new top-level forum")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Forum created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ForumResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Forum with this name already exists")
    })
    public ResponseEntity<ForumResponse> createForum(@Valid @RequestBody CreateForumRequest createForumRequest) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Forum forum = forumService.createForum(
                createForumRequest.getName(),
                createForumRequest.getDescription(),
                currentUser.get().getId());

        return new ResponseEntity<>(mapForumToDto(forum), HttpStatus.CREATED);
    }

    /**
     * Create a new subforum.
     *
     * @param parentId the parent forum ID
     * @param createForumRequest the forum creation request
     * @return the created subforum
     */
    @PostMapping("/{parentId}/subforums")
    @Operation(summary = "Create a new subforum under a parent forum")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subforum created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ForumResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to create subforums in this forum"),
            @ApiResponse(responseCode = "404", description = "Parent forum not found"),
            @ApiResponse(responseCode = "409", description = "Subforum with this name already exists in the parent forum")
    })
    public ResponseEntity<ForumResponse> createSubforum(
            @Parameter(description = "ID of the parent forum") @PathVariable Long parentId,
            @Valid @RequestBody CreateForumRequest createForumRequest) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Forum forum = forumService.createSubforum(
                createForumRequest.getName(),
                createForumRequest.getDescription(),
                parentId,
                currentUser.get().getId());

        return new ResponseEntity<>(mapForumToDto(forum), HttpStatus.CREATED);
    }

    /**
     * Get a forum by ID.
     *
     * @param id the forum ID
     * @return the forum
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get forum by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Forum found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ForumResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Forum not found")
    })
    public ResponseEntity<ForumResponse> getForumById(
            @Parameter(description = "ID of the forum to retrieve") @PathVariable Long id) {
        Optional<User> currentUser = userService.getCurrentUser();
        Long userId = currentUser.map(User::getId).orElse(null);

        Forum forum = forumService.getForumById(id);
        ForumResponse response = mapForumToDto(forum);
        
        // Add access information if user is authenticated
        if (userId != null) {
            boolean canRead = forumService.hasForumAccess(id, userId, AccessLevel.READ);
            boolean canWrite = forumService.hasForumAccess(id, userId, AccessLevel.WRITE);
            boolean canAdmin = forumService.hasForumAccess(id, userId, AccessLevel.ADMIN);
            
            response.setCanRead(canRead);
            response.setCanWrite(canWrite);
            response.setCanAdmin(canAdmin);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get all root forums.
     *
     * @return list of root forums
     */
    @GetMapping
    @Operation(summary = "Get all root forums")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ForumResponse.class)))})
    })
    public ResponseEntity<List<ForumResponse>> getRootForums() {
        List<Forum> forums = forumService.getRootForums();
        List<ForumResponse> response = forums.stream()
                .map(this::mapForumToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all subforums of a forum.
     *
     * @param parentId the parent forum ID
     * @return list of subforums
     */
    @GetMapping("/{parentId}/subforums")
    @Operation(summary = "Get all subforums of a parent forum")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ForumResponse.class)))}),
            @ApiResponse(responseCode = "404", description = "Parent forum not found")
    })
    public ResponseEntity<List<ForumResponse>> getSubforums(
            @Parameter(description = "ID of the parent forum") @PathVariable Long parentId) {
        List<Forum> forums = forumService.getSubforums(parentId);
        List<ForumResponse> response = forums.stream()
                .map(this::mapForumToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Update a forum.
     *
     * @param id the forum ID
     * @param updateForumRequest the forum update request
     * @return the updated forum
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a forum")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Forum updated successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ForumResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this forum"),
            @ApiResponse(responseCode = "404", description = "Forum not found"),
            @ApiResponse(responseCode = "409", description = "Forum name already in use")
    })
    public ResponseEntity<ForumResponse> updateForum(
            @Parameter(description = "ID of the forum to update") @PathVariable Long id,
            @Valid @RequestBody UpdateForumRequest updateForumRequest) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Forum forum = forumService.updateForum(
                id,
                updateForumRequest.getName(),
                updateForumRequest.getDescription(),
                currentUser.get().getId());

        return ResponseEntity.ok(mapForumToDto(forum));
    }

    /**
     * Delete a forum.
     *
     * @param id the forum ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a forum")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Forum deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this forum"),
            @ApiResponse(responseCode = "404", description = "Forum not found"),
            @ApiResponse(responseCode = "400", description = "Forum has subforums that must be deleted first")
    })
    public ResponseEntity<String> deleteForum(
            @Parameter(description = "ID of the forum to delete") @PathVariable Long id) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        forumService.deleteForum(id, currentUser.get().getId());
        return ResponseEntity.ok("Forum deleted successfully");
    }

    /**
     * Move a forum to a new parent.
     *
     * @param id the forum ID
     * @param newParentId the new parent forum ID (null for root level)
     * @return the moved forum
     */
    @PutMapping("/{id}/move")
    @Operation(summary = "Move a forum to a new parent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Forum moved successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ForumResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid operation (e.g., circular reference)"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Forum not found"),
            @ApiResponse(responseCode = "409", description = "Name conflict in destination")
    })
    public ResponseEntity<ForumResponse> moveForum(
            @Parameter(description = "ID of the forum to move") @PathVariable Long id,
            @Parameter(description = "ID of the new parent forum (null for root level)") 
            @RequestParam(required = false) Long newParentId) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Forum forum = forumService.moveForum(id, newParentId, currentUser.get().getId());
        return ResponseEntity.ok(mapForumToDto(forum));
    }

    /**
     * Search for forums.
     *
     * @param query the search query
     * @return list of matching forums
     */
    @GetMapping("/search")
    @Operation(summary = "Search for forums by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ForumResponse.class)))}),
            @ApiResponse(responseCode = "400", description = "Invalid search term")
    })
    public ResponseEntity<List<ForumResponse>> searchForums(
            @Parameter(description = "Search term") @RequestParam String query) {
        List<Forum> forums = forumService.searchForums(query);
        List<ForumResponse> response = forums.stream()
                .map(this::mapForumToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all forums accessible to the current user.
     *
     * @return list of accessible forums
     */
    @GetMapping("/accessible")
    @Operation(summary = "Get all forums the current user has access to")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ForumResponse.class)))}),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<ForumResponse>> getAccessibleForums() {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Forum> forums = forumService.getAccessibleForums(currentUser.get().getId());
        List<ForumResponse> response = forums.stream()
                .map(this::mapForumToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Grant a user access to a forum.
     *
     * @param id the forum ID
     * @param forumAccessRequest the access request
     * @return success message
     */
    @PostMapping("/{id}/access")
    @Operation(summary = "Grant a user access to a forum")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access granted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to manage access"),
            @ApiResponse(responseCode = "404", description = "Forum or user not found")
    })
    public ResponseEntity<String> grantForumAccess(
            @Parameter(description = "ID of the forum") @PathVariable Long id,
            @Valid @RequestBody ForumAccessRequest forumAccessRequest) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean success = forumService.grantForumAccess(
                id,
                forumAccessRequest.getUserId(),
                forumAccessRequest.getAccessLevel(),
                currentUser.get().getId());

        if (success) {
            return ResponseEntity.ok("Access granted successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to grant access");
        }
    }

    /**
     * Update a user's access to a forum.
     *
     * @param id the forum ID
     * @param forumAccessRequest the access request
     * @return success message
     */
    @PutMapping("/{id}/access")
    @Operation(summary = "Update a user's access to a forum")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to manage access"),
            @ApiResponse(responseCode = "404", description = "Forum, user, or access record not found")
    })
    public ResponseEntity<String> updateForumAccess(
            @Parameter(description = "ID of the forum") @PathVariable Long id,
            @Valid @RequestBody ForumAccessRequest forumAccessRequest) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean success = forumService.updateForumAccess(
                id,
                forumAccessRequest.getUserId(),
                forumAccessRequest.getAccessLevel(),
                currentUser.get().getId());

        if (success) {
            return ResponseEntity.ok("Access updated successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to update access");
        }
    }

    /**
     * Revoke a user's access to a forum.
     *
     * @param id the forum ID
     * @param userId the user ID
     * @return success message
     */
    @DeleteMapping("/{id}/access/{userId}")
    @Operation(summary = "Revoke a user's access to a forum")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access revoked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid operation (e.g., attempting to revoke the last admin)"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to manage access"),
            @ApiResponse(responseCode = "404", description = "Forum or user not found")
    })
    public ResponseEntity<String> revokeForumAccess(
            @Parameter(description = "ID of the forum") @PathVariable Long id,
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean success = forumService.revokeForumAccess(
                id,
                userId,
                currentUser.get().getId());

        if (success) {
            return ResponseEntity.ok("Access revoked successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to revoke access");
        }
    }

    /**
     * Map Forum entity to ForumResponse DTO.
     *
     * @param forum the forum entity
     * @return the forum DTO
     */
    private ForumResponse mapForumToDto(Forum forum) {
        ForumResponse.ForumResponseBuilder builder = ForumResponse.builder()
                .id(forum.getId())
                .name(forum.getName())
                .description(forum.getDescription())
                .createdAt(forum.getCreatedAt())
                .updatedAt(forum.getUpdatedAt())
                .postCount(forum.getPosts() != null ? forum.getPosts().size() : 0);

        // Add parent forum info if exists
        if (forum.getParentForum() != null) {
            builder.parentForumId(forum.getParentForum().getId())
                   .parentForumName(forum.getParentForum().getName());
        }

        // Add subforums if available
        if (forum.getSubForums() != null && !forum.getSubForums().isEmpty()) {
            List<ForumResponse> subForumDtos = forum.getSubForums().stream()
                    .map(this::mapForumToDto)
                    .collect(Collectors.toList());
            builder.subForums(subForumDtos);
        }

        return builder.build();
    }
}
