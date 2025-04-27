package com.example.forum.repository;

import com.example.forum.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Post entity operations.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    /**
     * Find all posts in a specific forum.
     *
     * @param forumId the ID of the forum
     * @param pageable pagination parameters
     * @return a page of posts
     */
    Page<Post> findByForumId(Long forumId, Pageable pageable);
    
    /**
     * Find all posts created by a specific user.
     *
     * @param userId the ID of the user
     * @param pageable pagination parameters
     * @return a page of posts
     */
    Page<Post> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find posts containing the given text in their title or content.
     *
     * @param searchTerm the text to search for
     * @param pageable pagination parameters
     * @return a page of posts matching the search
     */
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', ?1, '%'))")
    Page<Post> searchByTitleOrContent(String searchTerm, Pageable pageable);
    
    /**
     * Find posts in a specific forum containing the given text in their title or content.
     *
     * @param forumId the ID of the forum
     * @param searchTerm the text to search for
     * @param pageable pagination parameters
     * @return a page of posts matching the search
     */
    @Query("SELECT p FROM Post p WHERE p.forum.id = ?1 AND (LOWER(p.title) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', ?2, '%')))")
    Page<Post> searchByForumAndTitleOrContent(Long forumId, String searchTerm, Pageable pageable);
    
    /**
     * Count the number of posts in a forum.
     *
     * @param forumId the ID of the forum
     * @return the count of posts
     */
    long countByForumId(Long forumId);
    
    /**
     * Find all posts in forums that the user has access to.
     *
     * @param userId the ID of the user
     * @param pageable pagination parameters
     * @return a page of posts
     */
    @Query("SELECT p FROM Post p JOIN p.forum f JOIN f.forumAccesses fa WHERE fa.user.id = ?1")
    Page<Post> findPostsInAccessibleForums(Long userId, Pageable pageable);
}

