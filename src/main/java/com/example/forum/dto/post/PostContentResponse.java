package com.example.forum.dto.post;

import com.example.forum.model.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for post content response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostContentResponse {

    private Long id;
    private String filename;
    private String description;
    private ContentType contentType;
    private String contentUrl;
    private LocalDateTime createdAt;
}

