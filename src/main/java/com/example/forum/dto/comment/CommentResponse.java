package com.example.forum.dto.comment;

import com.example.forum.dto.user.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for comment responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object representing a comment")
public class CommentResponse {

    @Schema(description = "Unique identifier of the comment", example = "123")
    private Long id;
    
    @Schema(description = "Text content of the comment", example = "This is a great post!")
    private String content;
    
    @Schema(description = "Date and time when the comment was created", example = "2023-01-15T10:30:45")
    private LocalDateTime createdAt;
    
    @Schema(description = "Date and time when the comment was last updated", example = "2023-01-15T11:45:30")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Information about the comment author")
    private UserSummaryDto author;
    
    @Schema(description = "ID of the post this comment belongs to", example = "456")
    private Long postId;
    
    @Schema(description = "ID of the parent comment if this is a reply", example = "789", nullable = true)
    private Long parentCommentId;
    
    @Schema(description = "Number of replies to this comment", example = "5")
    private int replyCount;
    
    @Schema(description = "List of content attachments for this comment")
    private List<CommentContentResponse> contents;
    
    // Access information
    @Schema(description = "Whether the current user can edit this comment", example = "true")
    private boolean canEdit;
    
    @Schema(description = "Whether the current user can delete this comment", example = "true")
    private boolean canDelete;
}
