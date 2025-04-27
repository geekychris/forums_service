package com.example.forum.dto.comment;

import com.example.forum.model.ContentType;
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
public class CommentContentResponse {

    private Long id;
    private String filename;
    private String description;
    private ContentType contentType;
    private String contentUrl;
    private LocalDateTime createdAt;
}

