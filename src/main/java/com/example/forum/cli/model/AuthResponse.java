package com.example.forum.cli.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response object for authentication operations.
 * Contains the JWT token and related information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("tokenType")
    private String tokenType;
    
    @JsonProperty("expiresAt")
    private Instant expiresAt;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("username")
    private String username;
}

