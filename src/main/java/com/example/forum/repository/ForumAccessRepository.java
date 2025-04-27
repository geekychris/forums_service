package com.example.forum.repository;

import com.example.forum.model.AccessLevel;
import com.example.forum.model.ForumAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ForumAccess entity operations.
 */
@Repository
public interface ForumAccessRepository extends JpaRepository<ForumAccess, Long> {
    
    /**
     * Find a user's access to a specific forum.
     *
     * @param userId the ID of the user
     * @param forumId the ID of the forum
     * @return an Optional containing the forum access if found
     */
    Optional<ForumAccess> findByUserIdAndForumId(Long userId, Long forumId);
    
    /**
     * Find all forums a user has a specific access level to.
     *
     * @param userId the ID of the user
     * @param accessLevel the access level
     * @return a list of forum accesses
     */
    List<ForumAccess> findByUserIdAndAccessLevel(Long userId, AccessLevel accessLevel);
    
    /**
     * Find all users who have access to a specific forum.
     *
     * @param forumId the ID of the forum
     * @return a list of forum accesses
     */
    List<ForumAccess> findByForumId(Long forumId);
    
    /**
     * Find all forums a user has access to.
     *
     * @param userId the ID of the user
     * @return a list of forum accesses
     */
    List<ForumAccess> findByUserId(Long userId);
    
    /**
     * Check if a user has at least the specified access level to a forum.
     *
     * @param userId the ID of the user
     * @param forumId the ID of the forum
     * @param accessLevel the access level to check
     * @return true if the user has the access level, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(fa) > 0 THEN true ELSE false END FROM ForumAccess fa " +
           "WHERE fa.user.id = ?1 AND fa.forum.id = ?2 AND " +
           "(fa.accessLevel = ?3 OR " +
           "(fa.accessLevel = com.example.forum.model.AccessLevel.ADMIN AND ?3 = com.example.forum.model.AccessLevel.WRITE) OR " +
           "(fa.accessLevel = com.example.forum.model.AccessLevel.ADMIN AND ?3 = com.example.forum.model.AccessLevel.READ) OR " +
           "(fa.accessLevel = com.example.forum.model.AccessLevel.WRITE AND ?3 = com.example.forum.model.AccessLevel.READ))")
    boolean hasAccessLevel(Long userId, Long forumId, AccessLevel accessLevel);
    
    /**
     * Delete all access entries for a specific forum.
     *
     * @param forumId the ID of the forum
     */
    void deleteByForumId(Long forumId);
    
    /**
     * Delete all access entries for a specific user.
     *
     * @param userId the ID of the user
     */
    void deleteByUserId(Long userId);
}

