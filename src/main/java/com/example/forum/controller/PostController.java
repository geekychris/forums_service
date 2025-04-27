package com.example.forum.controller;

import com.example.forum.dto.post.CreatePostRequest;
import com.example.forum.dto.post.PostContentResponse;
import com.example.forum.dto.post.PostResponse;
import com.example.forum.dto.post.UpdatePostRequest;
import com.example.forum.dto.user.UserSummaryDto;
import com.example.forum.model.AccessLevel;
import com.example.forum.model.Content;
import com.example.forum.model.ContentType;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.service.ForumService;
import com.example.forum.service.PostService;
import com.example.forum.service.UserService;
import org.springframework.stereotype.Component;
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
 * Controller for managing posts.
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post management API")
public class PostController {

    private final PostService postService;
    private final ForumService forumService;
    private final UserService userService;

    /**
     * Create a new post.
     *
     * @param createPostRequest the post creation request
     * @return the created post
     */
    @PostMapping
    @Operation(summary = "Create a new post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to post in this forum"),
            @ApiResponse(responseCode = "404", description = "Forum not found")
    })
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest createPostRequest) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Post post = postService.createPost(
                createPostRequest.getTitle(),
                createPostRequest.getContent(),
                createPostRequest.getForumId(),
                currentUser.get().getId());

        return new ResponseEntity<>(mapPostToDto(post, currentUser.get()), HttpStatus.CREATED);
    }

    /**
     * Get a post by ID.
     *
     * @param id the post ID
     * @return the post
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post found",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<PostResponse> getPostById(
            @Parameter(description = "ID of the post to retrieve") @PathVariable Long id) {
        Optional<User> currentUser = userService.getCurrentUser();
        User user = currentUser.orElse(null);
        Long userId = user != null ? user.getId() : null;

        // If user is not logged in, this will throw AccessDeniedException if not readable
        Post post = postService.getPostById(id, userId);
        
        PostResponse response = mapPostToDto(post, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a post.
     *
     * @param id the post ID
     * @param updatePostRequest the post update request
     * @return the updated post
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "ID of the post to update") @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest updatePostRequest) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Post post = postService.updatePost(
                id,
                updatePostRequest.getTitle(),
                updatePostRequest.getContent(),
                currentUser.get().getId());

        return ResponseEntity.ok(mapPostToDto(post, currentUser.get()));
    }

    /**
     * Delete a post.
     *
     * @param id the post ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<String> deletePost(
            @Parameter(description = "ID of the post to delete") @PathVariable Long id) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        postService.deletePost(id, currentUser.get().getId());
        return ResponseEntity.ok("Post deleted successfully");
    }

    /**
     * Get posts by forum with pagination.
     *
     * @param forumId the forum ID
     * @param page the page number (0-based)
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @return paginated posts
     */
    @GetMapping("/by-forum/{forumId}")
    @Operation(summary = "Get posts by forum with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Not authorized to view posts in this forum"),
            @ApiResponse(responseCode = "404", description = "Forum not found")
    })
    public ResponseEntity<List<PostResponse>> getPostsByForum(
            @Parameter(description = "ID of the forum") @PathVariable Long forumId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String direction) {
        Optional<User> currentUser = userService.getCurrentUser();
        User user = currentUser.orElse(null);
        Long userId = user != null ? user.getId() : null;

        // Check if forum exists and user has access
        if (!forumService.hasForumAccess(forumId, userId, AccessLevel.READ)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        
        Page<Post> posts = postService.getPostsByForum(forumId, userId, pageable);
        
        List<PostResponse> response = posts.getContent().stream()
                .map(post -> mapPostToDto(post, user))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    /**
     * Search for posts.
     *
     * @param query the search query
     * @param forumId optional forum ID to scope the search
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated search results
     */
    @GetMapping("/search")
    @Operation(summary = "Search for posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid search term")
    })
    public ResponseEntity<List<PostResponse>> searchPosts(
            @Parameter(description = "Search term") @RequestParam String query,
            @Parameter(description = "Optional forum ID to scope the search") @RequestParam(required = false) Long forumId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Optional<User> currentUser = userService.getCurrentUser();
        User user = currentUser.orElse(null);
        Long userId = user != null ? user.getId() : null;

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;
        
        if (forumId != null) {
            // Search in specific forum
            posts = postService.searchPostsInForum(forumId, query, userId, pageable);
        } else {
            // Search across all forums user has access to
            posts = postService.searchPosts(query, userId, pageable);
        }
        
        List<PostResponse> response = posts.getContent().stream()
                .map(post -> mapPostToDto(post, user))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    /**
     * Get posts by user with pagination.
     *
     * @param userId the user ID
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated posts
     */
    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get posts by user with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class)))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<PostResponse>> getPostsByUser(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Optional<User> currentUser = userService.getCurrentUser();
        User user = currentUser.orElse(null);
        Long currentUserId = user != null ? user.getId() : null;

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Post> posts = postService.getPostsByUser(userId, currentUserId, pageable);
        
        List<PostResponse> response = posts.getContent().stream()
                .map(post -> mapPostToDto(post, user))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    /**
     * Upload content to a post.
     *
     * @param id the post ID
     * @param file the file to upload
     * @param description the content description
     * @param contentType the type of content
     * @param storeInDb whether to store the file in the database
     * @return the uploaded content
     */
    @PostMapping(value = "/{id}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload content to a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Content uploaded successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = PostContentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to add content to this post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<PostContentResponse> uploadContent(
            @Parameter(description = "ID of the post") @PathVariable Long id,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Content description") @RequestParam(required = false) String description,
            @Parameter(description = "Content type") @RequestParam ContentType contentType,
            @Parameter(description = "Store in database") @RequestParam(defaultValue = "false") boolean storeInDb) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Content content = postService.addPostContent(
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
     * Get all content for a post.
     *
     * @param id the post ID
     * @return list of content items
     */
    @GetMapping("/{id}/content")
    @Operation(summary = "Get all content for a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            array = @ArraySchema(schema = @Schema(implementation = PostContentResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<List<PostContentResponse>> getPostContent(
            @Parameter(description = "ID of the post") @PathVariable Long id) {
        Optional<User> currentUser = userService.getCurrentUser();
        User user = currentUser.orElse(null);
        Long userId = user != null ? user.getId() : null;

        List<Content> contents = postService.getPostContent(id, userId);
        List<PostContentResponse> response = contents.stream()
                .map(this::mapContentToDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    /**
     * Delete content from a post.
     *
     * @param postId the post ID
     * @param contentId the content ID
     * @return success message
     */
    @DeleteMapping("/{postId}/content/{contentId}")
    @Operation(summary = "Delete content from a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this content"),
            @ApiResponse(responseCode = "404", description = "Post or content not found")
    })
    public ResponseEntity<String> deletePostContent(
            @Parameter(description = "ID of the post") @PathVariable Long postId,
            @Parameter(description = "ID of the content to delete") @PathVariable Long contentId) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        postService.deletePostContent(contentId, currentUser.get().getId());
        return ResponseEntity.ok("Content deleted successfully");
    }

    /**
     * Map Post entity to PostResponse DTO.
     *
     * @param post the post entity
     * @param currentUser the current user (can be null)
     * @return the post DTO
     */
    private PostResponse mapPostToDto(Post post, User currentUser) {
        PostResponse.PostResponseBuilder builder = PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .forumId(post.getForum().getId())
                .forumName(post.getForum().getName())
                .commentCount(post.getComments() != null ? post.getComments().size() : 0);

        // Map author
        if (post.getUser() != null) {
            UserSummaryDto author = UserSummaryDto.builder()
                    .id(post.getUser().getId())
                    .username(post.getUser().getUsername())
                    .displayName(post.getUser().getDisplayName())
                    .build();
            builder.author(author);
        }

        // Map content
        if (post.getContents() != null && !post.getContents().isEmpty()) {
            List<PostContentResponse> contentResponses = post.getContents().stream()
                    .map(this::mapContentToDto)
                    .collect(Collectors.toList());
            builder.contents(contentResponses);
        }

        // Set permissions
        if (currentUser != null) {
            boolean isAuthor = post.getUser() != null && 
                    post.getUser().getId().equals(currentUser.getId());
            boolean isAdmin = forumService.hasForumAccess(
                    post.getForum().getId(), currentUser.getId(), AccessLevel.ADMIN);
            
            builder.canEdit(isAuthor || isAdmin);
            builder.canDelete(isAuthor || isAdmin);
        }

        return builder.build();
    }

    /**
     * Map Content entity to PostContentResponse DTO.
     *
     * @param content the content entity
     * @return the content DTO
     */
    private PostContentResponse mapContentToDto(Content content) {
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

        return PostContentResponse.builder()
                .id(content.getId())
                .filename(content.getFilename())
                .description(content.getDescription())
                .contentType(content.getContentType())
                .contentUrl(contentUrl)
                .createdAt(content.getCreatedAt())
                .build();
    }
}
