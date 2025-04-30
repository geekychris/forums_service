package com.forum.app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for comments.
 * Represents comment data returned from the API.
 */
public class CommentDto {
    
    private long id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDto author;
    private long postId;
    private Long parentCommentId;
    private int replyCount;
    private List<CommentContentDto> contents;
    private boolean canEdit;
    private boolean canDelete;
    
    /**
     * Default constructor.
     */
    public CommentDto() {
        this.contents = new ArrayList<>();
    }
    
    /**
     * Full constructor.
     */
    public CommentDto(long id, String content, LocalDateTime createdAt, 
            LocalDateTime updatedAt, UserDto author, long postId, 
            Long parentCommentId, int replyCount, boolean canEdit, boolean canDelete) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.author = author;
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.replyCount = replyCount;
        this.contents = new ArrayList<>();
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserDto getAuthor() {
        return author;
    }

    public void setAuthor(UserDto author) {
        this.author = author;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public List<CommentContentDto> getContents() {
        return contents;
    }

    public void setContents(List<CommentContentDto> contents) {
        this.contents = contents;
    }
    
    public void addContent(CommentContentDto content) {
        if (this.contents == null) {
            this.contents = new ArrayList<>();
        }
        this.contents.add(content);
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }
}

