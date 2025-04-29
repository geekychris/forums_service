package com.example.forum.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for comment creation requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new comment")
public class CreateCommentRequest {

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
    @Schema(description = "Content of the comment", example = "This is a great post!", required = true)
    private String content;

    @Schema(description = "ID of the post to comment on. Required for top-level comments.", example = "1")
    private Long postId;
    
    @Schema(description = "ID of the parent comment to reply to. Required for comment replies.", example = "2")
    private Long parentCommentId;
    
    /**
     * Validates that either postId or parentCommentId is provided, but not both.
     * @return true if validation passes
     */
    @AssertTrue(message = "Either postId or parentCommentId must be provided, but not both")
    public boolean isEitherPostIdOrParentCommentIdProvided() {
        return (postId != null && parentCommentId == null) || 
               (postId == null && parentCommentId != null);
    }
}
