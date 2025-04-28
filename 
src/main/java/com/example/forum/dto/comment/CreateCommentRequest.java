package com.example.forum.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
    private String content;
    
    @JsonProperty("postId")
    private Number postId; // For new comments on posts
    
    @JsonProperty("parentCommentId")
    private Number parentCommentId; // For replies to comments
    
    // Add getters to convert Number to Long when needed by the service layer
    public Long getPostId() {
        return postId != null ? postId.longValue() : null;
    }
    
    public Long getParentCommentId() {
        return parentCommentId != null ? parentCommentId.longValue() : null;
    }
}

