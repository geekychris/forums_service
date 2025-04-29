package com.example.forum.cli.commands;

import com.example.forum.cli.model.authentication.AuthResponse;
import com.example.forum.cli.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class AuthCommands {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @ShellMethod(value = "Login to the forum API", key = "login")
    public String login(
//            @ShellOption(help = "Your username") String username,
//            @ShellOption(help = "Your password") String password
            @Option(longNames = "username", shortNames = 'u', description = "username", required = true) String username,
            @Option(longNames = "password", shortNames = 'f', description = "Forum ID", required = true) String password
    ) {
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

    @ShellMethod(value = "Register a new user account", key = "register")
    public String register(
            @ShellOption(help = "Username") String username,
            @ShellOption(help = "Email address") String email,
            @ShellOption(help = "Password") String password,
            @ShellOption(help = "Display name", defaultValue = ShellOption.NULL) String displayName) {
        try {
            Object response = authService.register(username, email, password, displayName);
            return "Registration successful. You can now login.";
        } catch (Exception e) {
            return "Registration failed: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Logout and remove stored authentication token", key = "logout")
    public String logout() {
        authService.logout();
        return "Logged out successfully.";
    }

    @ShellMethod(value = "Display current user information", key = "whoami")
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
