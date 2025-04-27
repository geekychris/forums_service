package com.example.forum.service.impl;

import com.example.forum.exception.AccessDeniedException;
import com.example.forum.exception.BadRequestException;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.exception.StorageException;
import com.example.forum.model.*;
import com.example.forum.repository.ContentRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.service.ForumService;
import com.example.forum.service.PostService;
import com.example.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the PostService interface.
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ContentRepository contentRepository;
    private final ForumService forumService;
    private final UserService userService;

    @Value("${app.content.storage.path:./content-storage}")
    private String contentStoragePath;

    @Override
    @Transactional
    public Post createPost(String title, String content, Long forumId, Long userId) {
        // Validate input
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("Post title cannot be empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Post content cannot be empty");
        }

        // Check if user has at least WRITE access to the forum
        if (!forumService.hasForumAccess(forumId, userId, AccessLevel.WRITE)) {
            throw new AccessDeniedException("forum", "create posts in");
        }

        // Get user and forum
        User user = userService.getUserById(userId);
        Forum forum = forumService.getForumById(forumId);

        // Create and save the post
        Post post = Post.builder()
                .title(title)
                .content(content)
                .user(user)
                .forum(forum)
                .build();

        return postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Post getPostById(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        // Check if user has at least READ access to the forum
        if (!forumService.hasForumAccess(post.getForum().getId(), userId, AccessLevel.READ)) {
            throw new AccessDeniedException("post", "view");
        }

        return post;
    }

    @Override
    @Transactional
    public Post updatePost(Long id, String title, String content, Long userId) {
        // Get the post
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        // Check permissions - either user is the post author or has ADMIN access to the forum
        boolean isAuthor = post.getUser().getId().equals(userId);
        boolean isAdmin = forumService.hasForumAccess(post.getForum().getId(), userId, AccessLevel.ADMIN);

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("post", "update");
        }

        boolean changed = false;

        // Update title if provided
        if (title != null && !title.trim().isEmpty() && !title.equals(post.getTitle())) {
            post.setTitle(title);
            changed = true;
        }

        // Update content if provided
        if (content != null && !content.trim().isEmpty() && !content.equals(post.getContent())) {
            post.setContent(content);
            changed = true;
        }

        // Save if changes were made
        if (changed) {
            return postRepository.save(post);
        }

        return post;
    }

    @Override
    @Transactional
    public void deletePost(Long id, Long userId) {
        // Get the post
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        // Check permissions - either user is the post author or has ADMIN access to the forum
        boolean isAuthor = post.getUser().getId().equals(userId);
        boolean isAdmin = forumService.hasForumAccess(post.getForum().getId(), userId, AccessLevel.ADMIN);

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("post", "delete");
        }

        // Delete any content associated with the post
        List<Content> contents = contentRepository.findByPostId(id);
        for (Content content : contents) {
            // Delete file from disk if it's not stored in DB
            if (!content.isStoredInDb() && content.getContentPath() != null) {
                try {
                    Path filePath = Paths.get(content.getContentPath());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    // Log the error but continue with deletion
                    System.err.println("Failed to delete file: " + content.getContentPath());
                }
            }
        }

        // Delete all content records
        contentRepository.deleteByPostId(id);

        // Delete the post
        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Post> getPostsByForum(Long forumId, Long userId, Pageable pageable) {
        // Check if user has at least READ access to the forum
        if (!forumService.hasForumAccess(forumId, userId, AccessLevel.READ)) {
            throw new AccessDeniedException("forum", "view posts in");
        }

        return postRepository.findByForumId(forumId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Post> getPostsByUser(Long authorId, Long userId, Pageable pageable) {
        // Validate author exists
        userService.getUserById(authorId);

        // Get posts by author
        Page<Post> posts = postRepository.findByUserId(authorId, pageable);

        // Filter posts to only include those in forums the requesting user has access to
        // TODO: Implement forum access filtering logic
        
        return posts;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Post> searchPosts(String searchTerm, Long userId, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        // Search for posts
        Page<Post> posts = postRepository.searchByTitleOrContent(searchTerm, pageable);

        // Filter posts to only include those in forums the user has access to
        // TODO: Implement forum access filtering logic
        return posts;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Post> searchPostsInForum(Long forumId, String searchTerm, Long userId, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        // Check if user has at least READ access to the forum
        if (!forumService.hasForumAccess(forumId, userId, AccessLevel.READ)) {
            throw new AccessDeniedException("forum", "view posts in");
        }

        return postRepository.searchByForumAndTitleOrContent(forumId, searchTerm, pageable);
    }

    @Override
    @Transactional
    public Content addPostContent(Long postId, MultipartFile file, String description, 
                                 ContentType contentType, boolean storeInDb, Long userId) {
        // Get the post
        Post post = getPostById(postId, userId);

        // Check permissions - either user is the post author or has ADMIN access to the forum
        boolean isAuthor = post.getUser().getId().equals(userId);
        boolean hasWriteAccess = forumService.hasForumAccess(post.getForum().getId(), userId, AccessLevel.WRITE);

        if (!isAuthor && !hasWriteAccess) {
            throw new AccessDeniedException("post", "add content to");
        }

        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        try {
            // Generate a unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Create content object
            Content content = Content.builder()
                    .post(post)
                    .comment(null)
                    .filename(originalFilename)
                    .description(description)
                    .contentType(contentType)
                    .storedInDb(storeInDb)
                    .build();

            if (storeInDb) {
                // Store file in database
                content.setData(file.getBytes());
                content.setContentPath("db://" + uniqueFilename);
            } else {
                // Store file on disk
                Path storageDirectory = Paths.get(contentStoragePath);
                if (!Files.exists(storageDirectory)) {
                    Files.createDirectories(storageDirectory);
                }

                Path destination = storageDirectory.resolve(uniqueFilename);
                Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                content.setContentPath(destination.toString());
            }

            return contentRepository.save(content);
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Content> getPostContent(Long postId, Long userId) {
        // Check post exists and user has access
        getPostById(postId, userId);

        // Retrieve all content for the post
        return contentRepository.findByPostId(postId);
    }

    @Override
    @Transactional
    public void deletePostContent(Long contentId, Long userId) {
        // Get the content
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", contentId));

        // Ensure content is associated with a post
        if (content.getPost() == null) {
            throw new BadRequestException("Content is not associated with a post");
        }

        Post post = content.getPost();

        // Check permissions - either user is the post author or has ADMIN access to the forum
        boolean isAuthor = post.getUser().getId().equals(userId);
        boolean isAdmin = forumService.hasForumAccess(post.getForum().getId(), userId, AccessLevel.ADMIN);

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("post content", "delete");
        }

        // Delete file from disk if not stored in DB
        if (!content.isStoredInDb() && content.getContentPath() != null) {
            try {
                Path filePath = Paths.get(content.getContentPath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log the error but continue with deletion
                System.err.println("Failed to delete file: " + content.getContentPath());
            }
        }

        // Delete the content record
        contentRepository.delete(content);
    }
}
