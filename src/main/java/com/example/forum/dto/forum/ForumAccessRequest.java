package com.example.forum.dto.forum;

import com.example.forum.model.AccessLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumAccessRequest {
    @NotNull(message = "User ID is required")
    @JsonProperty("userId")
    private Number userId; // Use Number to accept both Integer and Long
    
    @NotNull(message = "Access level is required")
    private AccessLevel accessLevel;
    
    // Add getter to convert Number to Long when needed by the service layer
    public Long getUserId() {
        return userId != null ? userId.longValue() : null;
    }
}
