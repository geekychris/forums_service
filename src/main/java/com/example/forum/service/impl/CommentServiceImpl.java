package com.example.forum.service.impl;

import com.example.forum.exception.AccessDeniedException;
import com.example.forum.exception.BadRequestException;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.exception.StorageException;
import com.example.forum.model.*;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.ContentRepository;
import com.example.forum.service.CommentService;
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
 * Implementation of the CommentService interface.
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ContentRepository contentRepository;
    private final PostService postService;
    private final UserService userService;
    private final ForumService forumService;

    @Value("${app.content.storage.path:./content-storage}")
    private String contentStoragePath;

    @Override
    @Transactional
    public Comment createComment(Long postId, String content, Long userId) {
        // Validate input
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Comment content cannot be empty");
        }

        // Get the post (this will check read permissions)
        Post post = postService.getPostById(postId, userId);
        
        // Check if user has at least WRITE access to the forum
        if (!forumService.hasForumAccess(post.getForum().getId(), userId, AccessLevel.WRITE)) {
            throw new AccessDeniedException("You do not have permission to comment on this post");
        }
        // Get user
        User user = userService.getUserById(userId);

        // Create and save the comment
        Comment comment = Comment.builder()
                .content(content)
                .post(post)
                .user(user)
                .parentComment(null)
                .build();

        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment createReply(Long parentCommentId, String content, Long userId) {
        // Validate input
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Comment content cannot be empty");
        }

        // Get the parent comment
        Comment parentComment = getCommentById(parentCommentId, userId);
        Post post = parentComment.getPost();
        
        // Check if user has at least WRITE access to the forum
        if (!forumService.hasForumAccess(post.getForum().getId(), userId, AccessLevel.WRITE)) {
            throw new AccessDeniedException("You do not have permission to reply to this comment");
        }
        // Get user
        User user = userService.getUserById(userId);

        // Create and save the reply
        Comment reply = Comment.builder()
                .content(content)
                .post(post)
                .user(user)
                .parentComment(parentComment)
                .build();

        return commentRepository.save(reply);
    }
    @Override
    @Transactional(readOnly = true)
    public Comment getCommentById(Long id, Long userId) {
        // First get the comment
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        // Check if user has at least READ access to the forum
        if (!forumService.hasForumAccess(comment.getPost().getForum().getId(), userId, AccessLevel.READ)) {
            throw new AccessDeniedException("You do not have permission to view this comment");
        }

        return comment;
    }

    @Override
    @Transactional
    public Comment updateComment(Long id, String content, Long userId) {
        // Validate input
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Comment content cannot be empty");
        }

        // Get the comment
        Comment comment = getCommentById(id, userId);

        // Check permissions - either user is the comment author or has ADMIN access to the forum
        boolean isAuthor = comment.getUser().getId().equals(userId);
        boolean isAdmin = forumService.hasForumAccess(comment.getPost().getForum().getId(), userId, AccessLevel.ADMIN);
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to update this comment");
        }

        // Update content
        comment.setContent(content);
        
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long id, Long userId) {
        // Get the comment
        Comment comment = getCommentById(id, userId);

        // Check permissions - either user is the comment author or has ADMIN access to the forum
        boolean isAuthor = comment.getUser().getId().equals(userId);
        boolean isAdmin = forumService.hasForumAccess(comment.getPost().getForum().getId(), userId, AccessLevel.ADMIN);
        boolean isPostAuthor = comment.getPost().getUser().getId().equals(userId);
        if (!isAuthor && !isAdmin && !isPostAuthor) {
            throw new AccessDeniedException("You do not have permission to delete this comment");
        }

        // If the comment has replies, we need to handle them
        List<Comment> replies = commentRepository.findByParentCommentId(id, Pageable.unpaged()).getContent();
        
        // Option 1: Delete all replies recursively
        // This approach recursively deletes all nested comments
        for (Comment reply : replies) {
            deleteComment(reply.getId(), userId);
        }

        // Option 2: Orphan the replies (make them top-level comments)
        // Uncomment this block if you prefer to orphan replies instead of deleting them
        /*
        for (Comment reply : replies) {
            reply.setParentComment(null);
            commentRepository.save(reply);
        }
        */

        // Delete any content associated with the comment
        List<Content> contents = contentRepository.findByCommentId(id);
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
        contentRepository.deleteByCommentId(id);

        // Delete the comment
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> getCommentsByPost(Long postId, Long userId, Pageable pageable) {
        // Check post exists and user has access
        postService.getPostById(postId, userId);

        // Get all top-level comments for the post
        return commentRepository.findByPostIdAndParentCommentIsNull(postId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> getRepliesByComment(Long commentId, Long userId, Pageable pageable) {
        // Check comment exists and user has access
        getCommentById(commentId, userId);

        // Get all replies for the comment
        return commentRepository.findByParentCommentId(commentId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> getCommentsByUser(Long authorId, Long userId, Pageable pageable) {
        // Validate author exists
        userService.getUserById(authorId);

        // Get comments by author
        Page<Comment> comments = commentRepository.findByUserId(authorId, pageable);

        // Filter comments to only include those in forums the requesting user has access to
        // TODO: Implement forum access filtering logic
        
        return comments;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Comment> searchComments(String searchTerm, Long userId, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        // Search for comments
        Page<Comment> comments = commentRepository.searchByContent(searchTerm, pageable);

        // Filter comments to only include those in forums the user has access to
        // TODO: Implement forum access filtering logic
        
        return comments;
    }

    @Override
    @Transactional
    public Content addCommentContent(Long commentId, MultipartFile file, String description, 
                                    ContentType contentType, boolean storeInDb, Long userId) {
        // Get the comment
        Comment comment = getCommentById(commentId, userId);

        // Check permissions - either user is the comment author or has WRITE access to the forum
        boolean isAuthor = comment.getUser().getId().equals(userId);
        boolean hasWriteAccess = forumService.hasForumAccess(comment.getPost().getForum().getId(), userId, AccessLevel.WRITE);
        if (!isAuthor && !hasWriteAccess) {
            throw new AccessDeniedException("You do not have permission to add content to this comment");
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
                    .post(null)
                    .comment(comment)
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
    public List<Content> getCommentContent(Long commentId, Long userId) {
        // Check comment exists and user has access
        getCommentById(commentId, userId);

        // Retrieve all content for the comment
        return contentRepository.findByCommentId(commentId);
    }

    @Override
    @Transactional
    public void deleteCommentContent(Long contentId, Long userId) {
        // Get the content
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", contentId));

        // Ensure content is associated with a comment
        if (content.getComment() == null) {
            throw new BadRequestException("Content is not associated with a comment");
        }

        Comment comment = content.getComment();

        // Check permissions - either user is the comment author or has ADMIN access to the forum
        boolean isAuthor = comment.getUser().getId().equals(userId);
        boolean isAdmin = forumService.hasForumAccess(comment.getPost().getForum().getId(), userId, AccessLevel.ADMIN);
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete content from this comment");
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

    @Override
    @Transactional
    public Comment upvoteComment(Long commentId, Long userId) {
        // Get the comment
        Comment comment = getCommentById(commentId, userId);

        // Implementation would track user's vote and update the comment's vote count
        // For simplicity, we just return the comment
        return comment;
    }

    @Override
    @Transactional
    public Comment downvoteComment(Long commentId, Long userId) {
        // Get the comment
        Comment comment = getCommentById(commentId, userId);

        // Implementation would track user's vote and update the comment's vote count
        // For simplicity, we just return the comment
        return comment;
    }
}
