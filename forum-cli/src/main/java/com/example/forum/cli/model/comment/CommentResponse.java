package com.example.forum.cli.model.comment;

import com.example.forum.cli.model.content.ContentResponse;
import com.example.forum.cli.model.user.UserSummaryDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response model for comments.
 * Contains information about a comment including its metadata, author and associated content.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long postId;
    private Long parentCommentId;
    private int replyCount;
    private UserSummaryDto author;
    private List<ContentResponse> contents;
    private boolean canEdit;
    private boolean canDelete;
}
