package com.forum.app;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for comment content attachments.
 * Represents content data returned from the API.
 */
public class CommentContentDto {
    
    private long id;
    private String filename;
    private String description;
    private PostContentDto.ContentType contentType;
    private String contentUrl;
    private LocalDateTime createdAt;
    
    /**
     * Default constructor.
     */
    public CommentContentDto() {
    }
    
    /**
     * Full constructor.
     */
    public CommentContentDto(long id, String filename, String description, 
            PostContentDto.ContentType contentType, String contentUrl, LocalDateTime createdAt) {
        this.id = id;
        this.filename = filename;
        this.description = description;
        this.contentType = contentType;
        this.contentUrl = contentUrl;
        this.createdAt = createdAt;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PostContentDto.ContentType getContentType() {
        return contentType;
    }

    public void setContentType(PostContentDto.ContentType contentType) {
        this.contentType = contentType;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

