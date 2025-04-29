package com.example.forum.dto.comment;

import com.example.forum.model.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for comment content response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object representing content attached to a comment")
public class CommentContentResponse {

    @Schema(description = "Unique identifier of the content", example = "42")
    private Long id;
    
    @Schema(description = "Original filename of the attachment", example = "image.jpg")
    private String filename;
    
    @Schema(description = "Optional description of the content", example = "Screenshot of the issue", nullable = true)
    private String description;
    
    @Schema(description = "Type of the content (IMAGE, VIDEO, DOCUMENT, etc)")
    private ContentType contentType;
    
    @Schema(description = "URL to access the content", example = "http://example.com/api/content/42")
    private String contentUrl;
    
    @Schema(description = "Date and time when the content was uploaded", example = "2023-01-15T10:30:45")
    private LocalDateTime createdAt;
}
