package com.example.forum.service.impl;

import com.example.forum.exception.AccessDeniedException;
import com.example.forum.exception.BadRequestException;
import com.example.forum.exception.DuplicateResourceException;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.model.AccessLevel;
import com.example.forum.model.Forum;
import com.example.forum.model.ForumAccess;
import com.example.forum.model.User;
import com.example.forum.repository.ForumAccessRepository;
import com.example.forum.repository.ForumRepository;
import com.example.forum.service.ForumService;
import com.example.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the ForumService interface.
 */
@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements ForumService {

    private final ForumRepository forumRepository;
    private final ForumAccessRepository forumAccessRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Forum createForum(String name, String description, Long creatorId) {
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Forum name cannot be empty");
        }

        // Get creator user
        User creator = userService.getUserById(creatorId);
        
        // Check if a forum with this name already exists
        Optional<Forum> existingForum = forumRepository.findByNameIgnoreCase(name);
        if (existingForum.isPresent()) {
            throw new DuplicateResourceException("Forum", "name", name);
        }

        // Create and save the forum
        Forum forum = Forum.builder()
                .name(name)
                .description(description)
                .parentForum(null)
                .build();
        
        forum = forumRepository.save(forum);
        
        // Grant admin access to the creator
        ForumAccess access = ForumAccess.builder()
                .user(creator)
                .forum(forum)
                .accessLevel(AccessLevel.ADMIN)
                .build();
        
        forumAccessRepository.save(access);
        
        return forum;
    }

    @Override
    @Transactional
    public Forum createSubforum(String name, String description, Long parentId, Long creatorId) {
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Forum name cannot be empty");
        }

        // Check if parent forum exists first
        if (!forumRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Forum", "id", parentId);
        }

        // Check if user has admin access to parent forum before proceeding
        if (!hasForumAccess(parentId, creatorId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "create subforum in");
        }

        // Get creator user and parent forum after access check
        User creator = userService.getUserById(creatorId);
        Forum parentForum = getForumById(parentId);
        
        // Check if a subforum with this name already exists in the parent
        List<Forum> existingSubforums = forumRepository.findByParentForumId(parentId);
        if (existingSubforums.stream().anyMatch(f -> f.getName().equalsIgnoreCase(name))) {
            throw new DuplicateResourceException("Subforum", "name", name);
        }

        // Create and save the subforum
        Forum forum = Forum.builder()
                .name(name)
                .description(description)
                .parentForum(parentForum)
                .build();
        
        forum = forumRepository.save(forum);
        
        // Grant admin access to the creator
        ForumAccess access = ForumAccess.builder()
                .user(creator)
                .forum(forum)
                .accessLevel(AccessLevel.ADMIN)
                .build();
        
        forumAccessRepository.save(access);
        
        return forum;
    }

    @Override
    @Transactional(readOnly = true)
    public Forum getForumById(Long id) {
        return forumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forum", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Forum> getRootForums() {
        return forumRepository.findByParentForumIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Forum> getSubforums(Long parentId) {
        // Validate parent forum exists
        if (!forumRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Forum", "id", parentId);
        }
        
        return forumRepository.findByParentForumId(parentId);
    }

    @Override
    @Transactional
    public Forum updateForum(Long id, String name, String description, Long userId) {
        // Get the forum
        Forum forum = getForumById(id);
        
        // Check if user has admin access
        if (!hasForumAccess(id, userId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "update");
        }
        
        boolean changed = false;
        
        // Update name if provided
        if (name != null && !name.trim().isEmpty() && !name.equals(forum.getName())) {
            // Check if name is unique
            if (forum.getParentForum() == null) {
                // For root forums, check global uniqueness
                Optional<Forum> existingForum = forumRepository.findByNameIgnoreCase(name);
                if (existingForum.isPresent() && !existingForum.get().getId().equals(id)) {
                    throw new DuplicateResourceException("Forum", "name", name);
                }
            } else {
                // For subforums, check uniqueness within parent
                List<Forum> siblings = forumRepository.findByParentForumId(forum.getParentForum().getId());
                if (siblings.stream()
                        .anyMatch(f -> f.getName().equalsIgnoreCase(name) && !f.getId().equals(id))) {
                    throw new DuplicateResourceException("Subforum", "name", name);
                }
            }
            
            forum.setName(name);
            changed = true;
        }
        
        // Update description if provided
        if (description != null && !description.equals(forum.getDescription())) {
            forum.setDescription(description);
            changed = true;
        }
        
        // Save if changes were made
        if (changed) {
            return forumRepository.save(forum);
        }
        
        return forum;
    }

    @Override
    @Transactional
    public void deleteForum(Long id, Long userId) {
        // Get the forum
        Forum forum = getForumById(id);
        
        // Check if user has admin access
        if (!hasForumAccess(id, userId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "delete");
        }
        
        // Check if forum has subforums
        if (forumRepository.hasSubForums(id)) {
            throw new BadRequestException("Cannot delete forum with subforums. Delete subforums first or move them.");
        }
        
        // Delete all forum accesses
        forumAccessRepository.deleteByForumId(id);
        
        // Delete the forum
        forumRepository.delete(forum);
    }

    @Override
    @Transactional
    public Forum moveForum(Long id, Long newParentId, Long userId) {
        // Get the forum to move
        Forum forum = getForumById(id);
        
        // Check if user has admin access to the forum
        if (!hasForumAccess(id, userId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "move");
        }
        
        // If moving to root level
        if (newParentId == null) {
            // Check if a root forum with this name already exists
            Optional<Forum> existingForum = forumRepository.findByNameIgnoreCase(forum.getName());
            if (existingForum.isPresent() && !existingForum.get().getId().equals(id)) {
                throw new DuplicateResourceException("Forum", "name", forum.getName());
            }
            
            forum.setParentForum(null);
        } else {
            // Get the new parent forum
            Forum newParent = getForumById(newParentId);
            
            // Check if user has admin access to the new parent
            if (!hasForumAccess(newParentId, userId, AccessLevel.ADMIN)) {
                throw new AccessDeniedException("parent forum", "move to");
            }
            
            // Check if a subforum with this name already exists in the new parent
            List<Forum> existingSubforums = forumRepository.findByParentForumId(newParentId);
            if (existingSubforums.stream()
                    .anyMatch(f -> f.getName().equalsIgnoreCase(forum.getName()) && !f.getId().equals(id))) {
                throw new DuplicateResourceException("Subforum", "name", forum.getName());
            }
            
            // Check for circular reference
            Forum checkParent = newParent;
            while (checkParent != null) {
                if (checkParent.getId().equals(id)) {
                    throw new BadRequestException("Cannot move a forum to be a subforum of itself or one of its descendants");
                }
                checkParent = checkParent.getParentForum();
            }
            
            forum.setParentForum(newParent);
        }
        
        return forumRepository.save(forum);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Forum> searchForums(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }
        
        return forumRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Forum> getAccessibleForums(Long userId) {
        // Validate user exists
        userService.getUserById(userId);
        
        // Get all forum accesses for the user
        List<ForumAccess> accesses = forumAccessRepository.findByUserId(userId);
        
        // Extract the forums
        return accesses.stream()
                .map(ForumAccess::getForum)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Forum> getForumsByUserAccessLevel(Long userId, AccessLevel accessLevel) {
        // Validate user exists
        userService.getUserById(userId);
        
        // Get forum accesses with the specified access level
        List<ForumAccess> accesses = forumAccessRepository.findByUserIdAndAccessLevel(userId, accessLevel);
        
        // Extract the forums
        return accesses.stream()
                .map(ForumAccess::getForum)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public boolean hasForumAccess(Long forumId, Long userId, AccessLevel accessLevel) {
        // Validate user exists first
        try {
            userService.getUserById(userId);
        } catch (ResourceNotFoundException e) {
            return false;
        }

        // Check if forum exists
        Optional<Forum> forumOpt = forumRepository.findById(forumId);
        if (forumOpt.isEmpty()) {
            return false;
        }
        Forum forum = forumOpt.get();
        // Get the forum access for the user
        Optional<ForumAccess> userAccess = forumAccessRepository.findByUserIdAndForumId(userId, forumId);
        if (userAccess.isPresent()) {
            AccessLevel userLevel = userAccess.get().getAccessLevel();
            
            // ADMIN has all permissions
            if (userLevel == AccessLevel.ADMIN) {
                return true;
            }
            
            // WRITE has READ permission
            if (userLevel == AccessLevel.WRITE && accessLevel == AccessLevel.READ) {
                return true;
            }
            
            // Direct level match
            if (userLevel == accessLevel) {
                return true;
            }
        }

        // If no direct access and this forum has a parent, check parent forums recursively
        if (forum.getParentForum() != null) {
            return hasForumAccess(forum.getParentForum().getId(), userId, accessLevel);
        }
        
        return false;
    }

    @Override
    @Transactional
    public boolean grantForumAccess(Long forumId, Long userId, AccessLevel accessLevel, Long granterId) {
        // Get forum and users
        Forum forum = getForumById(forumId);
        User user = userService.getUserById(userId);
        
        // Check if granter has admin access
        if (!hasForumAccess(forumId, granterId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "manage access to");
        }
        
        // Check if user already has access
        Optional<ForumAccess> existingAccess = forumAccessRepository.findByUserIdAndForumId(userId, forumId);
        
        if (existingAccess.isPresent()) {
            // Update existing access
            ForumAccess access = existingAccess.get();
            access.setAccessLevel(accessLevel);
            forumAccessRepository.save(access);
        } else {
            // Create new access
            ForumAccess access = ForumAccess.builder()
                    .user(user)
                    .forum(forum)
                    .accessLevel(accessLevel)
                    .build();
            
            forumAccessRepository.save(access);
        }
        
        return true;
    }

    @Override
    @Transactional
    public boolean revokeForumAccess(Long forumId, Long userId, Long revokerId) {
        // Get forum
        Forum forum = getForumById(forumId);
        
        // Check if revoker has admin access
        if (!hasForumAccess(forumId, revokerId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "manage access to");
        }
        
        // Don't allow revoking your own admin access
        Optional<ForumAccess> accessToRevoke = forumAccessRepository.findByUserIdAndForumId(userId, forumId);
        if (accessToRevoke.isEmpty()) {
            // Access doesn't exist, so it's already "revoked"
            return true;
        }
        
        // Prevent removing the last admin
        if (accessToRevoke.get().getAccessLevel() == AccessLevel.ADMIN && userId.equals(revokerId)) {
            // Count how many admins the forum has
            long adminCount = forumAccessRepository.findByForumId(forumId).stream()
                    .filter(access -> access.getAccessLevel() == AccessLevel.ADMIN)
                    .count();
            
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot revoke your own admin access when you are the only admin");
            }
        }
        
        // Delete the access
        forumAccessRepository.delete(accessToRevoke.get());
        return true;
    }

    @Override
    @Transactional
    public boolean updateForumAccess(Long forumId, Long userId, AccessLevel accessLevel, Long updaterId) {
        // This is essentially the same as granting access, but we'll separate it for clarity in the API
        
        // Get forum and users
        Forum forum = getForumById(forumId);
        User user = userService.getUserById(userId);
        
        // Check if updater has admin access
        if (!hasForumAccess(forumId, updaterId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "manage access to");
        }
        
        // Check if user already has access
        Optional<ForumAccess> existingAccess = forumAccessRepository.findByUserIdAndForumId(userId, forumId);
        
        if (existingAccess.isEmpty()) {
            throw new ResourceNotFoundException("Forum access for user", "id", userId);
        }
        
        // Prevent downgrading the last admin
        if (existingAccess.get().getAccessLevel() == AccessLevel.ADMIN && 
                accessLevel != AccessLevel.ADMIN) {
            // Count how many admins the forum has
            long adminCount = forumAccessRepository.findByForumId(forumId).stream()
                    .filter(access -> access.getAccessLevel() == AccessLevel.ADMIN)
                    .count();
            
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot downgrade the last admin of a forum");
            }
        }
        
        // Update access level
        ForumAccess access = existingAccess.get();
        access.setAccessLevel(accessLevel);
        forumAccessRepository.save(access);
        
        return true;
    }
}
