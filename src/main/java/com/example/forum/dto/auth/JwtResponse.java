package com.example.forum.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for JWT authentication response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String accessToken;
    @Default
    private String tokenType = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String role;
}
