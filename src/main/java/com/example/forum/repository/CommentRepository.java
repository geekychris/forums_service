package com.example.forum.repository;

import com.example.forum.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Comment entity operations.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * Find all top-level comments for a specific post.
     *
     * @param postId the ID of the post
     * @param pageable pagination parameters
     * @return a page of comments
     */
    Page<Comment> findByPostIdAndParentCommentIsNull(Long postId, Pageable pageable);
    
    /**
     * Find all reply comments for a specific parent comment.
     *
     * @param parentCommentId the ID of the parent comment
     * @param pageable pagination parameters
     * @return a page of comments
     */
    Page<Comment> findByParentCommentId(Long parentCommentId, Pageable pageable);
    
    /**
     * Find all comments made by a specific user.
     *
     * @param userId the ID of the user
     * @param pageable pagination parameters
     * @return a page of comments
     */
    Page<Comment> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Count the number of comments for a post.
     *
     * @param postId the ID of the post
     * @return the count of comments
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = ?1")
    long countByPostId(Long postId);
    
    /**
     * Count the number of replies to a comment.
     *
     * @param commentId the ID of the parent comment
     * @return the count of replies
     */
    long countByParentCommentId(Long commentId);
    
    /**
     * Find comments containing the given text in their content.
     *
     * @param searchTerm the text to search for
     * @param pageable pagination parameters
     * @return a page of comments matching the search
     */
    @Query("SELECT c FROM Comment c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', ?1, '%'))")
    Page<Comment> searchByContent(String searchTerm, Pageable pageable);
}

