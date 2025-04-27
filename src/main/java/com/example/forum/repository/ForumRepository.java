package com.example.forum.repository;

import com.example.forum.model.Forum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Forum entity operations.
 */
@Repository
public interface ForumRepository extends JpaRepository<Forum, Long> {
    
    /**
     * Find all forums that have no parent (root forums).
     *
     * @return a list of all root forums
     */
    List<Forum> findByParentForumIsNull();
    
    /**
     * Find all subforums of a specific parent forum.
     *
     * @param parentId the ID of the parent forum
     * @return a list of all subforums
     */
    List<Forum> findByParentForumId(Long parentId);
    
    /**
     * Find forum by name (case insensitive).
     *
     * @param name the name to search for
     * @return an Optional containing the forum if found
     */
    Optional<Forum> findByNameIgnoreCase(String name);
    
    /**
     * Find forums containing the given text in their name.
     *
     * @param nameContains the text to search for in forum names
     * @return a list of forums matching the search
     */
    List<Forum> findByNameContainingIgnoreCase(String nameContains);
    
    /**
     * Check if a forum has any subforums.
     *
     * @param forumId the ID of the forum to check
     * @return true if the forum has subforums, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Forum f WHERE f.parentForum.id = ?1")
    boolean hasSubForums(Long forumId);
}

