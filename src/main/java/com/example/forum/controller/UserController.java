package com.example.forum.controller;

import com.example.forum.dto.user.UserProfileUpdateRequest;
import com.example.forum.dto.user.UserSummaryDto;
import com.example.forum.model.Role;
import com.example.forum.model.User;
import com.example.forum.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing users.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
public class UserController {

    private final UserService userService;

    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the user
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserSummaryDto.class))}),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserSummaryDto> getUserById(
            @Parameter(description = "ID of the user to retrieve") @PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(mapUserToDto(user));
    }

    /**
     * Update user profile.
     *
     * @param profileUpdateRequest the profile update request
     * @return the updated user
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserSummaryDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    public ResponseEntity<UserSummaryDto> updateProfile(@Valid @RequestBody UserProfileUpdateRequest profileUpdateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.updateUserProfile(
                currentUser.get().getId(),
                profileUpdateRequest.getDisplayName(),
                profileUpdateRequest.getEmail());

        return ResponseEntity.ok(mapUserToDto(user));
    }

    /**
     * Update user active status (admin only).
     *
     * @param id     the user ID
     * @param active the new active status
     * @return success message
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user active status (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> updateUserStatus(
            @Parameter(description = "ID of the user to update") @PathVariable Long id,
            @Parameter(description = "New active status") @RequestParam boolean active) {
        if (active) {
            userService.activateUser(id);
            return ResponseEntity.ok("User activated successfully");
        } else {
            userService.deactivateUser(id);
            return ResponseEntity.ok("User deactivated successfully");
        }
    }

    /**
     * Search for users.
     *
     * @param query the search query
     * @return list of matching users
     */
    @GetMapping("/search")
    @Operation(summary = "Search for users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<List<UserSummaryDto>> searchUsers(
            @Parameter(description = "Search term") @RequestParam String query) {
        List<User> users = userService.searchUsers(query);
        List<UserSummaryDto> userDtos = users.stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Map User entity to UserSummaryDto.
     *
     * @param user the user entity
     * @return the user DTO
     */
    private UserSummaryDto mapUserToDto(User user) {
        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole().name())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

