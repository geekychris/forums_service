package com.forum.app;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for users.
 * Represents user data returned from the API.
 */
public class UserDto {
    
    private long id;
    private String username;
    private String email;
    private String displayName;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;
    
    /**
     * Default constructor.
     */
    public UserDto() {
    }
    
    /**
     * Full constructor.
     */
    public UserDto(long id, String username, String email, String displayName, 
            String role, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return displayName != null && !displayName.isEmpty() ? displayName : username;
    }
}

