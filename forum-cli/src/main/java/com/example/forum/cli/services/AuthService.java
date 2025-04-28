package com.example.forum.cli.services;

import com.example.forum.cli.model.authentication.AuthRequest;
import com.example.forum.cli.model.authentication.AuthResponse;
import com.example.forum.cli.model.authentication.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final WebClient webClient;

    @Value("${forum.api.base-url}")
    private String baseUrl;

    @Value("${forum.api.auth-path}")
    private String authPath;

    @Value("${forum.auth.token-file}")
    private String tokenFilePath;
    public AuthResponse login(String username, String password) {
        AuthRequest request = AuthRequest.builder()
                .username(username)
                .password(password)
                .build();
        String uriPath = authPath + "/login";
        log.debug("Sending login request to: {}{}", baseUrl, uriPath);
        log.info("Attempting to login with username: {}", username);

        AuthResponse response = webClient.post()
                .uri(uriPath)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Login request to {} failed with status: {}", baseUrl + uriPath, e.getStatusCode());
                    
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        log.error("Authentication failed: Invalid credentials");
                    } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                        log.error("Authentication failed: Access denied");
                    } else if (e.getStatusCode() == HttpStatus.METHOD_NOT_ALLOWED) {
                        log.error("API error: Method not allowed. Try changing forum.api.auth-path in application.properties");
                        log.error("Tried path: {}", uriPath);
                    } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.error("API error: Endpoint not found. Try changing forum.api.auth-path in application.properties");
                        log.error("Tried path: {}", uriPath);
                    } else {
                        log.error("Login error: {} - {}", e.getStatusCode(), e.getMessage());
                    }
                    return Mono.empty();
                })
                .block();

        if (response != null && response.getAccessToken() != null) {
            saveToken(response.getAccessToken());
        }

        return response;
    }

    public Object register(String username, String email, String password, String displayName) {
        RegisterRequest request = RegisterRequest.builder()
                .username(username)
                .email(email)
                .password(password)
                .displayName(displayName)
                .build();

        String uriPath = authPath + "/register";
        log.debug("Sending registration request to: {}{}", baseUrl, uriPath);
        log.info("Attempting to register user: {}", username);

        return webClient.post()
                .uri(uriPath)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Registration request to {} failed with status: {}", baseUrl + uriPath, e.getStatusCode());
                    String responseBody = e.getResponseBodyAsString();
                    log.debug("Response body: {}", responseBody);
                    
                    if (e.getStatusCode() == HttpStatus.CONFLICT) {
                        log.error("Registration failed: Username or email already exists");
                    } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        log.error("API error: Authentication required. The forum service requires authentication for registration.");
                        log.error("Check if registration endpoint is correctly configured in the forum service.");
                    } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                        log.error("API error: Access denied. The forum service is denying access to the registration endpoint.");
                    } else if (e.getStatusCode() == HttpStatus.METHOD_NOT_ALLOWED) {
                        log.error("API error: Method not allowed. Try changing forum.api.auth-path in application.properties");
                        log.error("Tried path: {}", uriPath);
                    } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.error("API error: Endpoint not found. Try changing forum.api.auth-path in application.properties");
                        log.error("Tried path: {}", uriPath);
                    } else {
                        log.error("Registration error: {} - {}", e.getStatusCode(), e.getMessage());
                    }
                    return Mono.empty();
                })
                .block();
    }

    public Object getCurrentUser() {
        String uriPath = authPath + "/me";
        log.debug("Sending current user request to: {}{}", baseUrl, uriPath);

        return webClient.get()
                .uri(uriPath)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Current user request to {} failed with status: {}", baseUrl + uriPath, e.getStatusCode());
                    
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        log.error("Not authenticated: Please login first");
                    } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                        log.error("Access denied: Insufficient permissions");
                    } else if (e.getStatusCode() == HttpStatus.METHOD_NOT_ALLOWED) {
                        log.error("API error: Method not allowed. Try changing forum.api.auth-path in application.properties");
                        log.error("Tried path: {}", uriPath);
                    } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.error("API error: Endpoint not found. Try changing forum.api.auth-path in application.properties");
                        log.error("Tried path: {}", uriPath);
                    } else {
                        log.error("Error fetching current user: {}", e.getMessage());
                    }
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    log.error("General error fetching current user: {}", e.getMessage());
                    return Mono.empty();
                })
                .block();
    }

    public void logout() {
        try {
            Files.deleteIfExists(Paths.get(tokenFilePath));
            log.info("Logged out successfully. Token removed.");
        } catch (IOException e) {
            log.error("Error removing token file: {}", e.getMessage());
        }
    }

    /**
     * Check if the user is currently authenticated.
     *
     * @return true if a valid token exists, false otherwise
     */
    public boolean isAuthenticated() {
        return getToken().isPresent();
    }

    /**
     * Get the current authentication status.
     *
     * @return a string describing the authentication status
     */
    public String getAuthStatus() {
        if (isAuthenticated()) {
            Object currentUser = getCurrentUser();
            if (currentUser != null) {
                return "Authenticated";
            } else {
                return "Token exists but may be invalid";
            }
        }
        return "Not authenticated";
    }

    /**
     * Get the stored authentication token.
     *
     * @return the token if it exists
     */
    public Optional<String> getToken() {
        Path path = Paths.get(tokenFilePath);
        try {
            if (Files.exists(path)) {
                String token = Files.readString(path).trim();
                return token.isEmpty() ? Optional.empty() : Optional.of(token);
            }
        } catch (IOException e) {
            log.error("Error reading token file: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private void saveToken(String token) {
        try {
            Path path = Paths.get(tokenFilePath);
            // Create parent directories if they don't exist
            Files.createDirectories(path.getParent());
            Files.writeString(path, token);
            log.debug("Token saved to {}", tokenFilePath);
        } catch (IOException e) {
            log.error("Error saving token: {}", e.getMessage());
        }
    }
}
