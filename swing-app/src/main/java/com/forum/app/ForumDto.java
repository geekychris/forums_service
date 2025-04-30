package com.forum.app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for forums.
 * Represents forum data returned from the API.
 */
public class ForumDto {
    
    private long id;
    private String name;
    private String description;
    private Long parentForumId;
    private String parentForumName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int postCount;
    private List<ForumDto> subForums;
    private boolean canRead;
    private boolean canWrite;
    private boolean canAdmin;
    
    /**
     * Default constructor.
     */
    public ForumDto() {
        this.subForums = new ArrayList<>();
    }
    
    /**
     * Full constructor.
     * 
     * @param id Forum ID
     * @param name Forum name
     * @param description Forum description
     * @param parentForumId Parent forum ID (or null)
     * @param parentForumName Parent forum name (or null)
     * @param createdAt Creation timestamp
     * @param updatedAt Last update timestamp
     * @param postCount Number of posts
     * @param canRead Whether current user can read
     * @param canWrite Whether current user can write
     * @param canAdmin Whether current user can administrate
     */
    public ForumDto(long id, String name, String description, Long parentForumId, 
            String parentForumName, LocalDateTime createdAt, LocalDateTime updatedAt, 
            int postCount, boolean canRead, boolean canWrite, boolean canAdmin) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentForumId = parentForumId;
        this.parentForumName = parentForumName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.postCount = postCount;
        this.subForums = new ArrayList<>();
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.canAdmin = canAdmin;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getParentForumId() {
        return parentForumId;
    }

    public void setParentForumId(Long parentForumId) {
        this.parentForumId = parentForumId;
    }

    public String getParentForumName() {
        return parentForumName;
    }

    public void setParentForumName(String parentForumName) {
        this.parentForumName = parentForumName;
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

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public List<ForumDto> getSubForums() {
        return subForums;
    }

    public void setSubForums(List<ForumDto> subForums) {
        this.subForums = subForums;
    }
    
    public void addSubForum(ForumDto subForum) {
        if (this.subForums == null) {
            this.subForums = new ArrayList<>();
        }
        this.subForums.add(subForum);
    }

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public boolean isCanAdmin() {
        return canAdmin;
    }

    public void setCanAdmin(boolean canAdmin) {
        this.canAdmin = canAdmin;
    }
    
    @Override
    public String toString() {
        return name;
    }
}

