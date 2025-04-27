package com.example.forum.service;

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
import com.example.forum.service.impl.ForumServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ForumServiceTest {

    @Mock
    private ForumRepository forumRepository;

    @Mock
    private ForumAccessRepository forumAccessRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ForumServiceImpl forumService;

    private User testUser;
    private Forum testForum;
    private Forum testParentForum;
    private Forum testSubForum;
    private ForumAccess testAccess;

    @BeforeEach
    void setUp() {
        // Set up test data
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .active(true)
                .build();

        testForum = Forum.builder()
                .id(1L)
                .name("Test Forum")
                .description("Test forum description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .posts(new HashSet<>())
                .subForums(new HashSet<>())
                .build();

        testParentForum = Forum.builder()
                .id(2L)
                .name("Parent Forum")
                .description("Parent forum description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .posts(new HashSet<>())
                .subForums(new HashSet<>())
                .build();

        testSubForum = Forum.builder()
                .id(3L)
                .name("Sub Forum")
                .description("Sub forum description")
                .parentForum(testParentForum)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .posts(new HashSet<>())
                .subForums(new HashSet<>())
                .build();

        testAccess = ForumAccess.builder()
                .id(1L)
                .user(testUser)
                .forum(testForum)
                .accessLevel(AccessLevel.ADMIN)
                .grantedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    //
    // Create Forum Tests
    //

    @Test
    void testCreateForum_Success() {
        // Arrange
        String forumName = "New Forum";
        String description = "New forum description";
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(testUser);
        when(forumRepository.findByNameIgnoreCase(forumName)).thenReturn(Optional.empty());
        when(forumRepository.save(any(Forum.class))).thenAnswer(invocation -> {
            Forum savedForum = invocation.getArgument(0);
            savedForum.setId(10L);
            return savedForum;
        });
        when(forumAccessRepository.save(any(ForumAccess.class))).thenReturn(testAccess);

        // Act
        Forum result = forumService.createForum(forumName, description, userId);

        // Assert
        assertNotNull(result);
        assertEquals(forumName, result.getName());
        assertEquals(description, result.getDescription());
        assertNull(result.getParentForum());
        verify(forumRepository).save(any(Forum.class));
        verify(forumAccessRepository).save(any(ForumAccess.class));
    }

    @Test
    void testCreateForum_DuplicateName() {
        // Arrange
        String forumName = "Existing Forum";
        String description = "Description";
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(testUser);
        when(forumRepository.findByNameIgnoreCase(forumName)).thenReturn(Optional.of(testForum));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            forumService.createForum(forumName, description, userId);
        });
        verify(forumRepository, never()).save(any(Forum.class));
    }

    @Test
    void testCreateForum_EmptyName() {
        // Arrange
        String forumName = "";
        String description = "Description";
        Long userId = 1L;

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            forumService.createForum(forumName, description, userId);
        });
        verify(forumRepository, never()).save(any(Forum.class));
    }

    //
    // Create Subforum Tests
    //

    @Test
    void testCreateSubforum_Success() {
        // Arrange
        String forumName = "New Subforum";
        String description = "New subforum description";
        Long parentId = 2L;
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(testUser);
        when(forumRepository.findById(parentId)).thenReturn(Optional.of(testParentForum));
        when(forumAccessRepository.hasAccessLevel(userId, parentId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumRepository.findByParentForumId(parentId)).thenReturn(new ArrayList<>());
        when(forumRepository.save(any(Forum.class))).thenAnswer(invocation -> {
            Forum savedForum = invocation.getArgument(0);
            savedForum.setId(10L);
            return savedForum;
        });
        when(forumAccessRepository.save(any(ForumAccess.class))).thenReturn(testAccess);

        // Act
        Forum result = forumService.createSubforum(forumName, description, parentId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(forumName, result.getName());
        assertEquals(description, result.getDescription());
        assertNotNull(result.getParentForum());
        assertEquals(parentId, result.getParentForum().getId());
        verify(forumRepository).save(any(Forum.class));
        verify(forumAccessRepository).save(any(ForumAccess.class));
    }

    @Test
    void testCreateSubforum_NoAdminAccess() {
        // Arrange
        String forumName = "New Subforum";
        String description = "New subforum description";
        Long parentId = 2L;
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(testUser);
        when(forumRepository.findById(parentId)).thenReturn(Optional.of(testParentForum));
        when(forumAccessRepository.hasAccessLevel(userId, parentId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            forumService.createSubforum(forumName, description, parentId, userId);
        });
        verify(forumRepository, never()).save(any(Forum.class));
    }

    @Test
    void testCreateSubforum_DuplicateName() {
        // Arrange
        String forumName = "Existing Subforum";
        String description = "Description";
        Long parentId = 2L;
        Long userId = 1L;
        Forum existingSubforum = Forum.builder().id(5L).name(forumName).parentForum(testParentForum).build();

        when(userService.getUserById(userId)).thenReturn(testUser);
        when(forumRepository.findById(parentId)).thenReturn(Optional.of(testParentForum));
        when(forumAccessRepository.hasAccessLevel(userId, parentId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumRepository.findByParentForumId(parentId)).thenReturn(Collections.singletonList(existingSubforum));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            forumService.createSubforum(forumName, description, parentId, userId);
        });
        verify(forumRepository, never()).save(any(Forum.class));
    }

    //
    // Get Forum Tests
    //

    @Test
    void testGetForumById_Success() {
        // Arrange
        Long forumId = 1L;
        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));

        // Act
        Forum result = forumService.getForumById(forumId);

        // Assert
        assertNotNull(result);
        assertEquals(forumId, result.getId());
    }

    @Test
    void testGetForumById_NotFound() {
        // Arrange
        Long forumId = 999L;
        when(forumRepository.findById(forumId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            forumService.getForumById(forumId);
        });
    }

    @Test
    void testGetRootForums() {
        // Arrange
        List<Forum> rootForums = Arrays.asList(testForum, Forum.builder().id(3L).name("Another Forum").build());
        when(forumRepository.findByParentForumIsNull()).thenReturn(rootForums);

        // Act
        List<Forum> result = forumService.getRootForums();

        // Assert
        assertEquals(2, result.size());
        assertEquals(rootForums, result);
    }

    @Test
    void testGetSubforums() {
        // Arrange
        Long parentId = 2L;
        List<Forum> subforums = Collections.singletonList(testSubForum);
        when(forumRepository.existsById(parentId)).thenReturn(true);
        when(forumRepository.findByParentForumId(parentId)).thenReturn(subforums);

        // Act
        List<Forum> result = forumService.getSubforums(parentId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testSubForum.getId(), result.get(0).getId());
    }

    @Test
    void testGetSubforums_ParentNotFound() {
        // Arrange
        Long parentId = 999L;
        when(forumRepository.existsById(parentId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            forumService.getSubforums(parentId);
        });
        verify(forumRepository, never()).findByParentForumId(anyLong());
    }

    //
    // Update Forum Tests
    //

    @Test
    void testUpdateForum_Success() {
        // Arrange
        Long forumId = 1L;
        String newName = "Updated Forum";
        String newDescription = "Updated description";
        Long userId = 1L;

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(forumAccessRepository.hasAccessLevel(userId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumRepository.findByNameIgnoreCase(newName)).thenReturn(Optional.empty());
        when(forumRepository.save(any(Forum.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Forum result = forumService.updateForum(forumId, newName, newDescription, userId);

        // Assert
        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(newDescription, result.getDescription());
        verify(forumRepository).save(testForum);
    }

    @Test
    void testUpdateForum_NoAdminAccess() {
        // Arrange
        Long forumId = 1L;
        String newName = "Updated Forum";
        String newDescription = "Updated description";
        Long userId = 1L;

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(forumAccessRepository.hasAccessLevel(userId, forumId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            forumService.updateForum(forumId, newName, newDescription, userId);
        });
        verify(forumRepository, never()).save(any(Forum.class));
    }

    @Test
    void testUpdateForum_DuplicateName() {
        // Arrange
        Long forumId = 1L;
        String newName = "Duplicate Forum";
        String newDescription = "Updated description";
        Long userId = 1L;
        Forum existingForum = Forum.builder().id(3L).name(newName).build();

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(forumAccessRepository.hasAccessLevel(userId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumRepository.findByNameIgnoreCase(newName)).thenReturn(Optional.of(existingForum));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            forumService.updateForum(forumId, newName, newDescription, userId);
        });
        verify(forumRepository, never()).save(any(Forum.class));
    }

    //
    // Delete Forum Tests
    //

    @Test
    void testDeleteForum_Success() {
        // Arrange
        Long forumId = 1L;
        Long userId = 1L;

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(forumAccessRepository.hasAccessLevel(userId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumRepository.hasSubForums(forumId)).thenReturn(false);
        doNothing().when(forumAccessRepository).deleteByForumId(forumId);
        doNothing().when(forumRepository).delete(testForum);

        // Act
        forumService.deleteForum(forumId, userId);

        // Assert
        verify(forumAccessRepository).deleteByForumId(forumId);
        verify(forumRepository).delete(testForum);
    }

    @Test
    void testDeleteForum_NoAdminAccess() {
        // Arrange
        Long forumId = 1L;
        Long userId = 1L;

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(forumAccessRepository.hasAccessLevel(userId, forumId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            forumService.deleteForum(forumId, userId);
        });
        verify(forumRepository, never()).delete(any(Forum.class));
    }

    @Test
    void testDeleteForum_HasSubforums() {
        // Arrange
        Long forumId = 1L;
        Long userId = 1L;

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(forumAccessRepository.hasAccessLevel(userId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumRepository.hasSubForums(forumId)).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            forumService.deleteForum(forumId, userId);
        });
        verify(forumRepository, never()).delete(any(Forum.class));
    }

    //
    // Move Forum Tests
    //

    @Test
    void testMoveForum_ToRootSuccess() {
        // Arrange
        Long forumId = 3L;
        Long userId = 1L;
        testSubForum.setParentForum(testParentForum);

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testSubForum));
        when(forumAccessRepository.hasAccessLevel(userId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumRepository.findByNameIgnoreCase(testSubForum.getName())).thenReturn(Optional.empty());
        when(forumRepository.save(any(Forum.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Forum result = forumService.moveForum(forumId, null, userId);

        // Assert
        assertNotNull(result);
        assertNull(result.getParentForum());
        verify(forumRepository).save(any(Forum.class));
    }

    @Test
    void testMoveForum_ToNewParentSuccess() {
        // Arrange
        Long forumId = 1L;
        Long newParentId = 2L;
        Long userId = 1L;

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(forumRepository.findById(newParentId)).thenReturn(Optional.of(testParentForum));
        when(forumAccessRepository.hasAccessLevel(userId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumAccessRepository.hasAccessLevel(userId, newParentId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumRepository.findByParentForumId(newParentId)).thenReturn(new ArrayList<>());
        when(forumRepository.save(any(Forum.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Forum result = forumService.moveForum(forumId, newParentId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(newParentId, result.getParentForum().getId());
        verify(forumRepository).save(any(Forum.class));
    }

    @Test
    void testMoveForum_NoAccessToTarget() {
        // Arrange
        Long forumId = 1L;
        Long newParentId = 2L;
        Long userId = 1L;

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(forumRepository.findById(newParentId)).thenReturn(Optional.of(testParentForum));
        when(forumAccessRepository.hasAccessLevel(userId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumAccessRepository.hasAccessLevel(userId, newParentId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            forumService.moveForum(forumId, newParentId, userId);
        });
        verify(forumRepository, never()).save(any(Forum.class));
    }

    //
    // Search Forums Tests
    //

    @Test
    void testSearchForums_Success() {
        // Arrange
        String searchTerm = "Test";
        List<Forum> foundForums = Arrays.asList(testForum, testSubForum);

        when(forumRepository.findByNameContainingIgnoreCase(searchTerm)).thenReturn(foundForums);

        // Act
        List<Forum> result = forumService.searchForums(searchTerm);

        // Assert
        assertEquals(2, result.size());
        assertEquals(foundForums, result);
    }

    @Test
    void testSearchForums_EmptyTerm() {
        // Arrange
        String searchTerm = "";

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            forumService.searchForums(searchTerm);
        });
        verify(forumRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    //
    // Forum Access Tests
    //

    @Test
    void testGrantForumAccess_Success() {
        // Arrange
        Long forumId = 1L;
        Long userId = 2L;
        Long granterId = 1L;
        AccessLevel accessLevel = AccessLevel.WRITE;
        User targetUser = User.builder().id(userId).username("user2").build();

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(userService.getUserById(userId)).thenReturn(targetUser);
        when(forumAccessRepository.hasAccessLevel(granterId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumAccessRepository.findByUserIdAndForumId(userId, forumId)).thenReturn(Optional.empty());
        when(forumAccessRepository.save(any(ForumAccess.class))).thenReturn(testAccess);

        // Act
        boolean result = forumService.grantForumAccess(forumId, userId, accessLevel, granterId);

        // Assert
        assertTrue(result);
        verify(forumAccessRepository).save(any(ForumAccess.class));
    }

    @Test
    void testGrantForumAccess_NoAdminAccess() {
        // Arrange
        Long forumId = 1L;
        Long userId = 2L;
        Long granterId = 1L;
        AccessLevel accessLevel = AccessLevel.WRITE;

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(userService.getUserById(userId)).thenReturn(User.builder().id(userId).build());
        when(forumAccessRepository.hasAccessLevel(granterId, forumId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            forumService.grantForumAccess(forumId, userId, accessLevel, granterId);
        });
        verify(forumAccessRepository, never()).save(any(ForumAccess.class));
    }

    @Test
    void testRevokeForumAccess_Success() {
        // Arrange
        Long forumId = 1L;
        Long userId = 2L;
        Long revokerId = 1L;
        ForumAccess accessToRevoke = ForumAccess.builder()
                .id(2L)
                .user(User.builder().id(userId).build())
                .forum(testForum)
                .accessLevel(AccessLevel.WRITE)
                .build();

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(forumAccessRepository.hasAccessLevel(revokerId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumAccessRepository.findByUserIdAndForumId(userId, forumId)).thenReturn(Optional.of(accessToRevoke));
        doNothing().when(forumAccessRepository).delete(accessToRevoke);

        // Act
        boolean result = forumService.revokeForumAccess(forumId, userId, revokerId);

        // Assert
        assertTrue(result);
        verify(forumAccessRepository).delete(accessToRevoke);
    }

    @Test
    void testRevokeForumAccess_LastAdmin() {
        // Arrange
        Long forumId = 1L;
        Long userId = 1L; // Same as revoker
        Long revokerId = 1L;
        ForumAccess accessToRevoke = ForumAccess.builder()
                .id(1L)
                .user(testUser)
                .forum(testForum)
                .accessLevel(AccessLevel.ADMIN)
                .build();
        List<ForumAccess> forumAccesses = Collections.singletonList(accessToRevoke);

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(forumAccessRepository.hasAccessLevel(revokerId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumAccessRepository.findByUserIdAndForumId(userId, forumId)).thenReturn(Optional.of(accessToRevoke));
        when(forumAccessRepository.findByForumId(forumId)).thenReturn(forumAccesses);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            forumService.revokeForumAccess(forumId, userId, revokerId);
        });
        verify(forumAccessRepository, never()).delete(any(ForumAccess.class));
    }
    @Test
    void testUpdateForumAccess_Success() {
        // Arrange
        Long forumId = 1L;
        Long userId = 2L;
        Long updaterId = 1L;
        AccessLevel newAccessLevel = AccessLevel.READ;
        ForumAccess existingAccess = ForumAccess.builder()
                .id(2L)
                .user(User.builder().id(userId).build())
                .forum(testForum)
                .accessLevel(AccessLevel.WRITE)
                .build();

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(userService.getUserById(userId)).thenReturn(User.builder().id(userId).build());
        when(forumAccessRepository.hasAccessLevel(updaterId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumAccessRepository.findByUserIdAndForumId(userId, forumId)).thenReturn(Optional.of(existingAccess));
        when(forumAccessRepository.save(any(ForumAccess.class))).thenReturn(existingAccess);

        // Act
        boolean result = forumService.updateForumAccess(forumId, userId, newAccessLevel, updaterId);

        // Assert
        assertTrue(result);
        verify(forumAccessRepository).save(any(ForumAccess.class));
    }

    @Test
    void testUpdateForumAccess_LastAdmin() {
        // Arrange
        Long forumId = 1L;
        Long userId = 1L;
        Long updaterId = 1L;
        AccessLevel newAccessLevel = AccessLevel.READ; // Downgrading from ADMIN
        ForumAccess existingAccess = ForumAccess.builder()
                .id(1L)
                .user(testUser)
                .forum(testForum)
                .accessLevel(AccessLevel.ADMIN)
                .build();
        List<ForumAccess> forumAccesses = Collections.singletonList(existingAccess);

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(userService.getUserById(userId)).thenReturn(testUser);
        when(forumAccessRepository.hasAccessLevel(updaterId, forumId, AccessLevel.ADMIN)).thenReturn(true);
        when(forumAccessRepository.findByUserIdAndForumId(userId, forumId)).thenReturn(Optional.of(existingAccess));
        when(forumAccessRepository.findByForumId(forumId)).thenReturn(forumAccesses);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            forumService.updateForumAccess(forumId, userId, newAccessLevel, updaterId);
        });
        verify(forumAccessRepository, never()).save(any(ForumAccess.class));
    }

    @Test
    void testUpdateForumAccess_NoAdminAccess() {
        // Arrange
        Long forumId = 1L;
        Long userId = 2L;
        Long updaterId = 1L;
        AccessLevel newAccessLevel = AccessLevel.READ;

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(testForum));
        when(userService.getUserById(userId)).thenReturn(User.builder().id(userId).build());
        when(forumAccessRepository.hasAccessLevel(updaterId, forumId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            forumService.updateForumAccess(forumId, userId, newAccessLevel, updaterId);
        });
        verify(forumAccessRepository, never()).save(any(ForumAccess.class));
    }
}
