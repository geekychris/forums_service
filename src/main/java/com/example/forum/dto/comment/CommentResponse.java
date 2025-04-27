package com.example.forum.dto.comment;

import com.example.forum.dto.user.UserSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
/**
 * DTO for comment responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummaryDto author;
    private Long postId;
    private Long parentCommentId;
    private int replyCount;
    private List<CommentContentResponse> contents;
    
    // Access information
    private boolean canEdit;
    private boolean canDelete;
}

