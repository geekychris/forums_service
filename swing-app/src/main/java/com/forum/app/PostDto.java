package com.forum.app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for posts.
 * Represents post data returned from the API.
 */
public class PostDto {
    
    private long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDto author;
    private long forumId;
    private String forumName;
    private int commentCount;
    private List<PostContentDto> contents;
    private boolean canEdit;
    private boolean canDelete;
    
    /**
     * Default constructor.
     */
    public PostDto() {
        this.contents = new ArrayList<>();
    }
    
    /**
     * Full constructor.
     */
    public PostDto(long id, String title, String content, LocalDateTime createdAt,
            LocalDateTime updatedAt, UserDto author, long forumId, String forumName,
            int commentCount, boolean canEdit, boolean canDelete) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.author = author;
        this.forumId = forumId;
        this.forumName = forumName;
        this.commentCount = commentCount;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public long getForumId() {
        return forumId;
    }

    public void setForumId(long forumId) {
        this.forumId = forumId;
    }

    public String getForumName() {
        return forumName;
    }

    public void setForumName(String forumName) {
        this.forumName = forumName;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public List<PostContentDto> getContents() {
        return contents;
    }

    public void setContents(List<PostContentDto> contents) {
        this.contents = contents;
    }
    
    public void addContent(PostContentDto content) {
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
    
    @Override
    public String toString() {
        return title;
    }
}

