package com.example.forum.controller;

import com.example.forum.dto.comment.CommentContentResponse;
import com.example.forum.dto.comment.CommentResponse;
import com.example.forum.dto.comment.CreateCommentRequest;
import com.example.forum.dto.comment.UpdateCommentRequest;
import com.example.forum.dto.user.UserSummaryDto;
import com.example.forum.model.AccessLevel;
import com.example.forum.model.Comment;
import com.example.forum.model.Content;
import com.example.forum.model.ContentType;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.service.CommentService;
import com.example.forum.service.ForumService;
import com.example.forum.service.PostService;
import com.example.forum.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing comments.
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comment management API")
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final ForumService forumService;
    private final UserService userService;

    /**
     * Create a comment on a post.
     *
     * @param createCommentRequest the comment creation request
     * @return the created comment
    @PostMapping
    @PostMapping
    @Operation(summary = "Create a new comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to comment on this post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CreateCommentRequest createCommentRequest) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Comment comment;
        if (createCommentRequest.getPostId() != null) {
            // Create top-level comment on a post
            comment = commentService.createComment(
                    createCommentRequest.getPostId(),
                    createCommentRequest.getContent(),
                    currentUser.get().getId());
        } else if (createCommentRequest.getParentCommentId() != null) {
            // Create reply to a comment
            comment = commentService.createReply(
                    createCommentRequest.getParentCommentId(),
                    createCommentRequest.getContent(),
                    currentUser.get().getId());
        } else {
            return ResponseEntity.badRequest().build();
        }

        return new ResponseEntity<>(mapCommentToDto(comment, currentUser.get()), HttpStatus.CREATED);
    }

    /**
     * Get a comment by ID.
     *
     * @param id the comment ID
     * @return the comment
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get comment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment found",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<CommentResponse> getCommentById(
            @Parameter(description = "ID of the comment to retrieve") @PathVariable Long id) {
        Optional<User> currentUser = userService.getCurrentUser();
        User user = currentUser.orElse(null);
        Long userId = user != null ? user.getId() : null;

        Comment comment = commentService.getCommentById(id, userId);
        CommentResponse response = mapCommentToDto(comment, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a comment.
     *
     * @param id the comment ID
     * @param updateCommentRequest the comment update request
     * @return the updated comment
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<CommentResponse> updateComment(
            @Parameter(description = "ID of the comment to update") @PathVariable Long id,
            @Valid @RequestBody UpdateCommentRequest updateCommentRequest) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Comment comment = commentService.updateComment(
                id,
                updateCommentRequest.getContent(),
                currentUser.get().getId());

        return ResponseEntity.ok(mapCommentToDto(comment, currentUser.get()));
    }

    /**
     * Delete a comment.
     *
     * @param id the comment ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<String> deleteComment(
            @Parameter(description = "ID of the comment to delete") @PathVariable Long id) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        commentService.deleteComment(id, currentUser.get().getId());
        return ResponseEntity.ok("Comment deleted successfully");
    }

    /**
     * Get comments for a post with pagination.
     *
     * @param postId the post ID
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated comments
     */
    @GetMapping("/by-post/{postId}")
    @Operation(summary = "Get comments by post with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            array = @ArraySchema(schema = @Schema(implementation = CommentResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(
            @Parameter(description = "ID of the post") @PathVariable Long postId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Optional<User> currentUser = userService.getCurrentUser();
        User user = currentUser.orElse(null);
        Long userId = user != null ? user.getId() : null;

        // Check post exists and get its forum ID for permission check
        Post post = postService.getPostById(postId, userId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "createdAt");
        Page<Comment> comments = commentService.getCommentsByPost(postId, userId, pageable);
        
        List<CommentResponse> response = comments.getContent().stream()
                .map(comment -> mapCommentToDto(comment, user))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    /**
     * Get replies to a comment with pagination.
     *
     * @param commentId the parent comment ID
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated replies
     */
    @GetMapping("/{commentId}/replies")
    @Operation(summary = "Get replies to a comment with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            array = @ArraySchema(schema = @Schema(implementation = CommentResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<List<CommentResponse>> getRepliesByComment(
            @Parameter(description = "ID of the parent comment") @PathVariable Long commentId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Optional<User> currentUser = userService.getCurrentUser();
        User user = currentUser.orElse(null);
        Long userId = user != null ? user.getId() : null;

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "createdAt");
        Page<Comment> replies = commentService.getRepliesByComment(commentId, userId, pageable);
        
        List<CommentResponse> response = replies.getContent().stream()
                .map(comment -> mapCommentToDto(comment, user))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    /**
     * Get comments by user with pagination.
     *
     * @param userId the user ID
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated comments
     */
    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get comments by user with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            array = @ArraySchema(schema = @Schema(implementation = CommentResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this user's comments"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Optional<User> currentUser = userService.getCurrentUser();
        User user = currentUser.orElse(null);
        Long currentUserId = user != null ? user.getId() : null;

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Comment> comments = commentService.getCommentsByUser(userId, currentUserId, pageable);
        
        List<CommentResponse> response = comments.getContent().stream()
                .map(comment -> mapCommentToDto(comment, user))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    /**
     * Search for comments.
     *
     * @param query the search query
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated search results
     */
    @GetMapping("/search")
    @Operation(summary = "Search for comments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            array = @ArraySchema(schema = @Schema(implementation = CommentResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid search term"),
            @ApiResponse(responseCode = "403", description = "Not authorized to search comments")
    })
    public ResponseEntity<List<CommentResponse>> searchComments(
            @Parameter(description = "Search term") @RequestParam String query,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Optional<User> currentUser = userService.getCurrentUser();
        User user = currentUser.orElse(null);
        Long userId = user != null ? user.getId() : null;

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.searchComments(query, userId, pageable);
        
        List<CommentResponse> response = comments.getContent().stream()
                .map(comment -> mapCommentToDto(comment, user))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }
    /**
     * Upload content to a comment.
     *
     * @param id the comment ID
     * @param file the file to upload
     * @param description the content description
     * @param contentType the type of content
     * @param storeInDb whether to store the file in the database
     * @return the uploaded content
     */
    @PostMapping(value = "/{id}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload content to a comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Content uploaded successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = CommentContentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to add content to this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<CommentContentResponse> uploadContent(
            @Parameter(description = "ID of the comment") @PathVariable Long id,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Content description") @RequestParam(required = false) String description,
            @Parameter(description = "Content type") @RequestParam ContentType contentType,
            @Parameter(description = "Store in database") @RequestParam(defaultValue = "false") boolean storeInDb) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Content content = commentService.addCommentContent(
                id, 
                file, 
                description, 
                contentType, 
                storeInDb, 
                currentUser.get().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapContentToDto(content));
    }
    
    /**
     * Get all content for a comment.
     *
     * @param id the comment ID
     * @return list of content items
     */
    @GetMapping("/{id}/content")
    @Operation(summary = "Get all content for a comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            array = @ArraySchema(schema = @Schema(implementation = CommentContentResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<List<CommentContentResponse>> getCommentContent(
            @Parameter(description = "ID of the comment") @PathVariable Long id) {
        Optional<User> currentUser = userService.getCurrentUser();
        User user = currentUser.orElse(null);
        Long userId = user != null ? user.getId() : null;

        List<Content> contents = commentService.getCommentContent(id, userId);
        List<CommentContentResponse> response = contents.stream()
                .map(this::mapContentToDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }
    
    /**
     * Upvote a comment.
     *
     * @param id the comment ID
     * @return the updated comment
     */
    @PostMapping("/{id}/upvote")
    @Operation(summary = "Upvote a comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment upvoted successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "User already upvoted this comment"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to upvote comments"),
            @ApiResponse(responseCode = "404", description = "Comment not found"),
            @ApiResponse(responseCode = "409", description = "User already voted on this comment")
    })
    public ResponseEntity<CommentResponse> upvoteComment(
            @Parameter(description = "ID of the comment to upvote") @PathVariable Long id) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Comment comment = commentService.upvoteComment(id, currentUser.get().getId());
        return ResponseEntity.ok(mapCommentToDto(comment, currentUser.get()));
    }
    
    /**
     * Downvote a comment.
     *
     * @param id the comment ID
     * @return the updated comment
     */
    @PostMapping("/{id}/downvote")
    @Operation(summary = "Downvote a comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment downvoted successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to downvote comments"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<CommentResponse> downvoteComment(
            @Parameter(description = "ID of the comment to downvote") @PathVariable Long id) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Comment comment = commentService.downvoteComment(id, currentUser.get().getId());
        return ResponseEntity.ok(mapCommentToDto(comment, currentUser.get()));
    }

    /**
     * Delete content from a comment.
     *
     * @param commentId the comment ID
     * @param contentId the content ID
     * @return success message
     */
    @DeleteMapping("/{commentId}/content/{contentId}")
    @Operation(summary = "Delete content from a comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this content"),
            @ApiResponse(responseCode = "404", description = "Comment or content not found")
    })
    public ResponseEntity<String> deleteCommentContent(
            @Parameter(description = "ID of the comment") @PathVariable Long commentId,
            @Parameter(description = "ID of the content to delete") @PathVariable Long contentId) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        commentService.deleteCommentContent(contentId, currentUser.get().getId());
        return ResponseEntity.ok("Content deleted successfully");
    }

    /**
     * Map Comment entity to CommentResponse DTO.
     *
     * @param comment the comment entity
     * @param currentUser the current user (can be null)
     * @return the comment DTO
     */
    private CommentResponse mapCommentToDto(Comment comment, User currentUser) {
        CommentResponse.CommentResponseBuilder builder = CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .postId(comment.getPost().getId());

        // Set parent comment ID if it's a reply
        if (comment.getParentComment() != null) {
            builder.parentCommentId(comment.getParentComment().getId());
        }

        // Count replies
        int replyCount = comment.getReplies() != null ? comment.getReplies().size() : 0;
        builder.replyCount(replyCount);

        // Map author
        if (comment.getUser() != null) {
            UserSummaryDto author = UserSummaryDto.builder()
                    .id(comment.getUser().getId())
                    .username(comment.getUser().getUsername())
                    .displayName(comment.getUser().getDisplayName())
                    .build();
            builder.author(author);
        }

        // Map content
        if (comment.getContents() != null && !comment.getContents().isEmpty()) {
            List<CommentContentResponse> contentResponses = comment.getContents().stream()
                    .map(this::mapContentToDto)
                    .collect(Collectors.toList());
            builder.contents(contentResponses);
        }

        // Set permissions
        if (currentUser != null) {
            boolean isAuthor = comment.getUser() != null && 
                    comment.getUser().getId().equals(currentUser.getId());
            boolean isPostAuthor = comment.getPost().getUser().getId().equals(currentUser.getId());
            boolean isAdmin = forumService.hasForumAccess(
                    comment.getPost().getForum().getId(), currentUser.getId(), AccessLevel.ADMIN);
            
            builder.canEdit(isAuthor || isAdmin);
            builder.canDelete(isAuthor || isPostAuthor || isAdmin);
        }

        return builder.build();
    }

    /**
     * Map Content entity to CommentContentResponse DTO.
     *
     * @param content the content entity
     * @return the content DTO
     */
    private CommentContentResponse mapContentToDto(Content content) {
        String contentUrl = null;
        if (!content.isStoredInDb() && content.getContentPath() != null) {
            // Generate URL for content stored on disk
            contentUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/content/")
                    .path(content.getFilename())
                    .toUriString();
        } else if (content.isStoredInDb()) {
            // Generate URL for content stored in DB
            contentUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/content/")
                    .path(content.getId().toString())
                    .toUriString();
        }

        return CommentContentResponse.builder()
                .id(content.getId())
                .filename(content.getFilename())
                .description(content.getDescription())
                .contentType(content.getContentType())
                .contentUrl(contentUrl)
                .createdAt(content.getCreatedAt())
                .build();
    }
}
