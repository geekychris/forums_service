package com.example.forum.cli.model.authentication;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Response object returned after successful authentication.
 * Contains the JWT token and user details.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String id;
    private String username;
    private String email;
    private String displayName;
    private String role;
}
