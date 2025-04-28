package com.example.forum.cli.services;

import com.example.forum.cli.model.AuthResponse;
import com.example.forum.cli.model.LoginRequest;
import com.example.forum.cli.model.RegisterRequest;
import com.example.forum.cli.model.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service for handling authentication operations with the forum API.
 */
@Service
@Slf4j
public class AuthService {

    private final WebClient webClient;
    
    public AuthService(@Qualifier("cliWebClient") WebClient webClient) {
        this.webClient = webClient;
    }
    
    @Value("${forum.api.base-url}")
    private String baseUrl;
    
    @Value("${forum.api.auth-path:/api/auth}")
    private String authPath;
    
    @Value("${forum.auth.token-file:${user.home}/.forum-cli/token}")
    private String tokenFilePath;

    /**
     * Login to the forum API.
     *
     * @param username the username
     * @param password the password
     * @return Mono containing auth response with token
     */
    public Mono<AuthResponse> login(String username, String password) {
        String uriPath = authPath + "/login";
        log.debug("Sending login request to: {}{}", baseUrl, uriPath);
        
        LoginRequest request = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();
        
        return webClient.post()
                .uri(uriPath)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .doOnSuccess(response -> {
                    log.info("Login successful for user: {}", username);
                    saveToken(response.getToken());
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        log.error("Authentication failed: Invalid credentials");
                    } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                        log.error("Authentication failed: Access denied");
                    } else if (e.getStatusCode() == HttpStatus.METHOD_NOT_ALLOWED) {
                        log.error("API error: Method not allowed. The endpoint may be incorrect: {}", uriPath);
                    } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.error("API error: Endpoint not found: {}", uriPath);
                    } else {
                        log.error("Login error: {} - {}", e.getStatusCode(), e.getMessage());
                    }
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    log.error("General login error: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Register a new user.
     *
     * @param username the username
     * @param email the email
     * @param password the password
     * @param displayName the display name
     * @return Mono containing auth response with token
     */
    public Mono<AuthResponse> register(String username, String email, String password, String displayName) {
        String uriPath = authPath + "/register";
        RegisterRequest request = RegisterRequest.builder()
                .username(username)
                .email(email)
                .password(password)
                .displayName(displayName)
                .build();
        
        return webClient.post()
                .uri(uriPath)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .doOnSuccess(response -> {
                    log.info("Registration successful for user: {}", username);
                    saveToken(response.getToken());
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Registration request to {} failed with status: {}", baseUrl + uriPath, e.getStatusCode());
                    
                    if (e.getStatusCode() == HttpStatus.CONFLICT) {
                        log.error("Registration failed: Username or email already exists");
                    } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        log.error("Registration failed: Authentication required");
                    } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                        log.error("Registration failed: Access denied");
                    } else if (e.getStatusCode() == HttpStatus.METHOD_NOT_ALLOWED) {
                        log.error("API error: Method not allowed. The endpoint may be incorrect: {}", uriPath);
                    } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.error("API error: Endpoint not found: {}", uriPath);
                    } else {
                        log.error("Registration error: {} - {}", e.getStatusCode(), e.getMessage());
                    }
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    log.error("General registration error: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Get the current user profile.
     *
     * @return Mono containing user profile
     */
    public Mono<UserProfile> getCurrentUser() {
        String uriPath = authPath + "/me";
        log.debug("Sending current user request to: {}{}", baseUrl, uriPath);
        
        String token = getToken();
        if (token == null || token.isEmpty()) {
            log.error("No authentication token found. Please login first.");
            return Mono.empty();
        }
        
        return webClient.get()
                .uri(uriPath)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(UserProfile.class)
                .doOnSuccess(profile -> log.info("Retrieved user profile for: {}", profile.getUsername()))
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        log.error("Not authenticated: Please login first");
                    } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                        log.error("Access denied: Insufficient permissions");
                    } else if (e.getStatusCode() == HttpStatus.METHOD_NOT_ALLOWED) {
                        log.error("API error: Method not allowed. The endpoint may be incorrect: {}", uriPath);
                    } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.error("API error: Endpoint not found: {}", uriPath);
                    } else {
                        log.error("Error fetching current user: {}", e.getMessage());
                    }
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    log.error("General error fetching current user: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Save authentication token to file.
     *
     * @param token the JWT token
     */
    private void saveToken(String token) {
        try {
            Path path = Paths.get(tokenFilePath);
            Path parent = path.getParent();
            
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            
            Files.writeString(path, token);
            log.debug("Authentication token saved to: {}", tokenFilePath);
        } catch (Exception e) {
            log.error("Failed to save authentication token: {}", e.getMessage());
        }
    }

    /**
     * Read authentication token from file.
     *
     * @return the JWT token or null if not found
     */
    public String getToken() {
        try {
            Path path = Paths.get(tokenFilePath);
            if (Files.exists(path)) {
                return Files.readString(path).trim();
            }
        } catch (Exception e) {
            log.error("Failed to read authentication token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Log out by deleting the saved token.
     *
     * @return true if logout was successful
     */
    public boolean logout() {
        try {
            Path path = Paths.get(tokenFilePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Logged out successfully");
                return true;
            }
            return true; // Already logged out
        } catch (Exception e) {
            log.error("Failed to logout: {}", e.getMessage());
            return false;
        }
    }
}
