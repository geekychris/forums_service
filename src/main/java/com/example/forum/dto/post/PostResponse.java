package com.example.forum.dto.post;

import com.example.forum.dto.user.UserSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for post response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummaryDto author;
    private Long forumId;
    private String forumName;
    private int commentCount;
    private List<PostContentResponse> contents;
    
    // Access information
    private boolean canEdit;
    private boolean canDelete;
}

