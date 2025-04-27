package com.example.forum.dto.forum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for forum response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumResponse {

    private Long id;
    private String name;
    private String description;
    private Long parentForumId;
    private String parentForumName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer postCount;
    private List<ForumResponse> subForums;
    
    // Access levels - only populated when user is authenticated
    private boolean canRead;
    private boolean canWrite;
    private boolean canAdmin;
}
