package com.example.forum.service;

import com.example.forum.model.Comment;
import com.example.forum.model.Content;
import com.example.forum.model.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for managing comment-related operations.
 */
public interface CommentService {

    /**
     * Create a top-level comment on a post.
     *
     * @param postId  the ID of the post to comment on
     * @param content the comment content
     * @param userId  the ID of the user creating the comment
     * @return the created comment
     */
    Comment createComment(Long postId, String content, Long userId);

    /**
     * Create a reply to another comment.
     *
     * @param parentCommentId the ID of the parent comment to reply to
     * @param content         the comment content
     * @param userId          the ID of the user creating the reply
     * @return the created comment
     */
    Comment createReply(Long parentCommentId, String content, Long userId);

    /**
     * Get a comment by ID.
     *
     * @param id     the comment ID
     * @param userId the ID of the user requesting the comment
     * @return the comment
     */
    Comment getCommentById(Long id, Long userId);

    /**
     * Update a comment.
     *
     * @param id      the comment ID
     * @param content the new content
     * @param userId  the ID of the user updating the comment
     * @return the updated comment
     */
    Comment updateComment(Long id, String content, Long userId);

    /**
     * Delete a comment.
     *
     * @param id     the comment ID
     * @param userId the ID of the user deleting the comment
     */
    void deleteComment(Long id, Long userId);

    /**
     * Get all top-level comments for a post, with pagination.
     *
     * @param postId   the post ID
     * @param userId   the ID of the user requesting the comments
     * @param pageable the pagination information
     * @return a page of comments
     */
    Page<Comment> getCommentsByPost(Long postId, Long userId, Pageable pageable);

    /**
     * Get all replies to a comment, with pagination.
     *
     * @param commentId the parent comment ID
     * @param userId    the ID of the user requesting the replies
     * @param pageable  the pagination information
     * @return a page of comments
     */
    Page<Comment> getRepliesByComment(Long commentId, Long userId, Pageable pageable);

    /**
     * Get all comments made by a user, with pagination.
     *
     * @param authorId the author user ID
     * @param userId   the ID of the user requesting the comments
     * @param pageable the pagination information
     * @return a page of comments
     */
    Page<Comment> getCommentsByUser(Long authorId, Long userId, Pageable pageable);

    /**
     * Delete a comment content.
     *
     * @param contentId the content ID
     * @param userId the user ID
     */
    void deleteCommentContent(Long contentId, Long userId);

    /**
     * Upvote a comment.
     *
     * @param commentId the comment ID
     * @param userId the user ID
     * @return the updated comment
     */
    Comment upvoteComment(Long commentId, Long userId);

    /**
     * Downvote a comment.
     *
     * @param commentId the comment ID
     * @param userId the user ID
     * @return the updated comment
     */
    Comment downvoteComment(Long commentId, Long userId);

    /**
     * Search for comments by content.
     *
     * @param searchTerm the search term
     * @param userId the ID of the user performing the search
     * @param pageable the pagination information
     * @return a page of comments matching the search term
     */
    Page<Comment> searchComments(String searchTerm, Long userId, Pageable pageable);

    /**
     * Add content to a comment.
     *
     * @param commentId   the comment ID
     * @param file        the file to upload
     * @param description the content description
     * @param contentType the type of content
     * @param storeInDb   whether to store the file in the database
     * @param userId      the ID of the user adding the content
     * @return the added content
     */
    Content addCommentContent(Long commentId, MultipartFile file, String description, 
                             ContentType contentType, boolean storeInDb, Long userId);

    /**
     * Get all content for a comment.
     *
     * @param commentId the ID of the comment
     * @param userId the ID of the user requesting the content
     * @return list of content items for the comment
     */
    List<Content> getCommentContent(Long commentId, Long userId);
}

