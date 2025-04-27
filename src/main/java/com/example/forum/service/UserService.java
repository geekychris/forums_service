package com.example.forum.service;

import com.example.forum.model.Role;
import com.example.forum.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing user-related operations.
 */
public interface UserService {

    /**
     * Register a new user.
     *
     * @param username    the username
     * @param password    the password
     * @param email       the email
     * @param displayName the display name
     * @return the created user
     */
    User registerUser(String username, String password, String email, String displayName);

    /**
     * Register a new user with a specified role.
     *
     * @param username    the username
     * @param password    the password
     * @param email       the email
     * @param displayName the display name
     * @param role        the role
     * @return the created user
     */
    User registerUser(String username, String password, String email, String displayName, Role role);

    /**
     * Authenticate a user.
     *
     * @param username the username
     * @param password the password
     * @return true if authentication is successful, false otherwise
     */
    boolean authenticateUser(String username, String password);

    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the user
     */
    User getUserById(Long id);

    /**
     * Get a user by username.
     *
     * @param username the username
     * @return the user
     */
    User getUserByUsername(String username);

    /**
     * Get a user by email.
     *
     * @param email the email
     * @return the user
     */
    User getUserByEmail(String email);

    /**
     * Check if a username exists.
     *
     * @param username the username
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email exists.
     *
     * @param email the email
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Update a user's profile.
     *
     * @param id          the user ID
     * @param displayName the new display name (null if not changing)
     * @param email       the new email (null if not changing)
     * @return the updated user
     */
    User updateUserProfile(Long id, String displayName, String email);

    /**
     * Update a user's password.
     *
     * @param id          the user ID
     * @param oldPassword the old password
     * @param newPassword the new password
     * @return true if successful, false otherwise
     */
    boolean updateUserPassword(Long id, String oldPassword, String newPassword);

    /**
     * Change a user's role.
     *
     * @param id   the user ID
     * @param role the new role
     * @return the updated user
     */
    User changeUserRole(Long id, Role role);

    /**
     * Deactivate a user account.
     *
     * @param id the user ID
     */
    void deactivateUser(Long id);

    /**
     * Activate a user account.
     *
     * @param id the user ID
     */
    void activateUser(Long id);

    /**
     * Search for users by username or display name.
     *
     * @param searchTerm the search term
     * @return a list of matching users
     */
    List<User> searchUsers(String searchTerm);

    /**
     * Get the currently authenticated user.
     *
     * @return the current user, or null if not authenticated
     */
    Optional<User> getCurrentUser();
}

