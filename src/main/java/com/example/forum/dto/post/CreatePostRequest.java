package com.example.forum.dto.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Content is required")
    @Size(min = 10, max = 10000, message = "Content must be between 10 and 10000 characters")
    private String content;
    
    @NotNull(message = "Forum ID is required")
    @JsonProperty("forumId")
    private Number forumId; // Use Number to accept both Integer and Long
    
    // Add getter to convert Number to Long when needed by the service layer
    public Long getForumId() {
        return forumId != null ? forumId.longValue() : null;
    }
}

