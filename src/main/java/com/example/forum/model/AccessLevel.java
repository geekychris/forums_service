package com.example.forum.model;

/**
 * Enum representing different access levels users can have to forums.
 */
public enum AccessLevel {
    READ,       // Can only read posts and comments
    WRITE,      // Can read and create posts and comments
    ADMIN       // Full administrative access to the forum
}

