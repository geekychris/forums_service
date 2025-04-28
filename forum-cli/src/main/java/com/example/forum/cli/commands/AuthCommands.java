package com.example.forum.cli.commands;

import com.example.forum.cli.model.authentication.AuthResponse;
import com.example.forum.cli.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.ShellComponent;

@ShellComponent
@Command(group = "Authentication")
@RequiredArgsConstructor
public class AuthCommands {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Command(command = "login", description = "Login to the forum API")
    public String login(
            @Option(longNames = "username", shortNames = 'u', description = "Your username", required = true) String username,
            @Option(longNames = "password", shortNames = 'p', description = "Your password", required = true) String password) {
        try {
            AuthResponse response = authService.login(username, password);
            if (response != null && response.getAccessToken() != null) {
                return String.format("Successfully logged in as %s (%s)", 
                        response.getUsername(), 
                        response.getDisplayName() != null ? response.getDisplayName() : "");
            } else {
                return "Login failed. No token received.";
            }
        } catch (Exception e) {
            return "Login failed: " + e.getMessage();
        }
    }

    @Command(command = "register", description = "Register a new user account")
    public String register(
            @Option(longNames = "username", shortNames = 'u', description = "Username", required = true) String username,
            @Option(longNames = "email", shortNames = 'e', description = "Email address", required = true) String email,
            @Option(longNames = "password", shortNames = 'p', description = "Password", required = true) String password,
            @Option(longNames = "display-name", shortNames = 'd', description = "Display name") String displayName) {
        try {
            Object response = authService.register(username, email, password, displayName);
            return "Registration successful. You can now login.";
        } catch (Exception e) {
            return "Registration failed: " + e.getMessage();
        }
    }

    @Command(command = "logout", description = "Logout and remove stored authentication token")
    public String logout() {
        authService.logout();
        return "Logged out successfully.";
    }

    @Command(command = "whoami", description = "Display current user information")
    public String whoami() {
        try {
            Object userInfo = authService.getCurrentUser();
            if (userInfo != null) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userInfo);
            } else {
                return "Not logged in or unable to retrieve user information.";
            }
        } catch (Exception e) {
            return "Error retrieving user information: " + e.getMessage();
        }
    }
}
