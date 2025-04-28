package com.example.forum.cli.model.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response model for content items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentResponse {
    private Long id;
    private String filename;
    private String description;
    private String contentType;
    private String contentUrl;
    private LocalDateTime createdAt;
}
