package com.example.forum.cli.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * User profile information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("displayName")
    private String displayName;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("role")
    private String role;
    
    @JsonProperty("active")
    private boolean active;
    
    @JsonProperty("createdAt")
    private Instant createdAt;
    
    @JsonProperty("updatedAt")
    private Instant updatedAt;
    
    @JsonProperty("postCount")
    private Integer postCount;
    
    @JsonProperty("commentCount")
    private Integer commentCount;
}

