package com.example.forum.repository;

import com.example.forum.model.Content;
import com.example.forum.model.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Content entity operations.
 */
@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    
    /**
     * Find all content attached to a specific post.
     *
     * @param postId the ID of the post
     * @return a list of content items
     */
    List<Content> findByPostId(Long postId);
    
    /**
     * Find all content attached to a specific comment.
     *
     * @param commentId the ID of the comment
     * @return a list of content items
     */
    List<Content> findByCommentId(Long commentId);
    
    /**
     * Find all content of a specific type attached to a post.
     *
     * @param postId the ID of the post
     * @param contentType the type of content to find
     * @return a list of content items
     */
    List<Content> findByPostIdAndContentType(Long postId, ContentType contentType);
    
    /**
     * Find all content of a specific type attached to a comment.
     *
     * @param commentId the ID of the comment
     * @param contentType the type of content to find
     * @return a list of content items
     */
    List<Content> findByCommentIdAndContentType(Long commentId, ContentType contentType);
    
    /**
     * Find content by filename.
     *
     * @param filename the filename to search for
     * @return an Optional containing the content if found
     */
    Optional<Content> findByFilename(String filename);
    
    /**
     * Delete all content attached to a specific post.
     *
     * @param postId the ID of the post
     */
    void deleteByPostId(Long postId);
    
    /**
     * Delete all content attached to a specific comment.
     *
     * @param commentId the ID of the comment
     */
    void deleteByCommentId(Long commentId);
}

