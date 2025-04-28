package com.example.forum.cli.model.post;

import com.example.forum.cli.model.content.ContentResponse;
import com.example.forum.cli.model.user.UserSummaryDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Forum context
    private Long forumId;
    private String forumName;
    
    // Author information
    private UserSummaryDto author;
    
    // Content and comments
    private List<ContentResponse> contents;
    private int commentCount;
    
    // Permissions
    private boolean canEdit;
    private boolean canDelete;
}
