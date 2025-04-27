package com.example.forum.model;

/**
 * Enum representing user roles in the system.
 * Used for authorization and permission checks.
 */
public enum Role {
    USER,       // Regular user with basic permissions
    MODERATOR,  // User with elevated permissions for content moderation
    ADMIN       // User with full administrative access
}
