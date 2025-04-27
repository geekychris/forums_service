package com.example.forum.service;

import com.example.forum.model.AccessLevel;
import com.example.forum.model.Forum;

import java.util.List;

/**
 * Service interface for managing forum-related operations.
 */
public interface ForumService {

    /**
     * Create a new forum.
     *
     * @param name        the forum name
     * @param description the forum description
     * @param creatorId   the ID of the user creating the forum
     * @return the created forum
     */
    Forum createForum(String name, String description, Long creatorId);

    /**
     * Create a new subforum within a parent forum.
     *
     * @param name        the forum name
     * @param description the forum description
     * @param parentId    the ID of the parent forum
     * @param creatorId   the ID of the user creating the forum
     * @return the created forum
     */
    Forum createSubforum(String name, String description, Long parentId, Long creatorId);

    /**
     * Get a forum by ID.
     *
     * @param id the forum ID
     * @return the forum
     */
    Forum getForumById(Long id);

    /**
     * Get all root forums (forums without a parent).
     *
     * @return a list of root forums
     */
    List<Forum> getRootForums();

    /**
     * Get all subforums of a parent forum.
     *
     * @param parentId the ID of the parent forum
     * @return a list of subforums
     */
    List<Forum> getSubforums(Long parentId);

    /**
     * Update a forum's details.
     *
     * @param id          the forum ID
     * @param name        the new name (null if not changing)
     * @param description the new description (null if not changing)
     * @param userId      the ID of the user making the update
     * @return the updated forum
     */
    Forum updateForum(Long id, String name, String description, Long userId);

    /**
     * Delete a forum.
     *
     * @param id     the forum ID
     * @param userId the ID of the user making the deletion
     */
    void deleteForum(Long id, Long userId);

    /**
     * Move a forum to a different parent.
     *
     * @param id         the forum ID
     * @param newParentId the ID of the new parent forum (null for root level)
     * @param userId     the ID of the user performing the move
     * @return the updated forum
     */
    Forum moveForum(Long id, Long newParentId, Long userId);

    /**
     * Search for forums by name.
     *
     * @param searchTerm the search term
     * @return a list of matching forums
     */
    List<Forum> searchForums(String searchTerm);

    /**
     * Get all forums accessible to a user.
     *
     * @param userId the user ID
     * @return a list of accessible forums
     */
    List<Forum> getAccessibleForums(Long userId);

    /**
     * Get all forums where a user has a specific access level.
     *
     * @param userId      the user ID
     * @param accessLevel the required access level
     * @return a list of forums
     */
    List<Forum> getForumsByUserAccessLevel(Long userId, AccessLevel accessLevel);

    /**
     * Check if a user has a specific access level to a forum.
     *
     * @param forumId     the forum ID
     * @param userId      the user ID
     * @param accessLevel the access level to check
     * @return true if the user has the access level, false otherwise
     */
    boolean hasForumAccess(Long forumId, Long userId, AccessLevel accessLevel);

    /**
     * Grant a user access to a forum.
     *
     * @param forumId     the forum ID
     * @param userId      the user ID to grant access to
     * @param accessLevel the access level to grant
     * @param granterId   the ID of the user granting the access
     * @return true if successful, false otherwise
     */
    boolean grantForumAccess(Long forumId, Long userId, AccessLevel accessLevel, Long granterId);

    /**
     * Revoke a user's access to a forum.
     *
     * @param forumId   the forum ID
     * @param userId    the user ID to revoke access from
     * @param revokerId the ID of the user revoking the access
     * @return true if successful, false otherwise
     */
    boolean revokeForumAccess(Long forumId, Long userId, Long revokerId);

    /**
     * Update a user's access level to a forum.
     *
     * @param forumId     the forum ID
     * @param userId      the user ID to update
     * @param accessLevel the new access level
     * @param updaterId   the ID of the user updating the access
     * @return true if successful, false otherwise
     */
    boolean updateForumAccess(Long forumId, Long userId, AccessLevel accessLevel, Long updaterId);
}

