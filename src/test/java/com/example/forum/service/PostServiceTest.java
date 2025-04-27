package com.example.forum.service;

import com.example.forum.exception.AccessDeniedException;
import com.example.forum.exception.BadRequestException;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.model.AccessLevel;
import com.example.forum.model.Content;
import com.example.forum.model.ContentType;
import com.example.forum.model.Forum;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.repository.ContentRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private ForumService forumService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PostServiceImpl postService;

    private User testUser;
    private Forum testForum;
    private Post testPost;
    private Content testContent;
    private MultipartFile testFile;

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
                .build();

        testPost = Post.builder()
                .id(1L)
                .title("Test Post")
                .content("Test post content")
                .user(testUser)
                .forum(testForum)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .comments(new HashSet<>())
                .contents(new HashSet<>())
                .build();

        testContent = Content.builder()
                .id(1L)
                .filename("testimage.jpg")
                .contentPath("/path/to/file")
                .contentType(ContentType.IMAGE)
                .description("Test image")
                .storedInDb(false)
                .post(testPost)
                .createdAt(LocalDateTime.now())
                .build();

        testFile = new MockMultipartFile(
                "file",
                "testimage.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    // Create Post Tests

    @Test
    void testCreatePost_Success() {
        // Arrange
        String title = "New Post";
        String content = "New post content";
        Long forumId = 1L;
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(testUser);
        when(forumService.getForumById(forumId)).thenReturn(testForum);
        when(forumService.hasForumAccess(forumId, userId, AccessLevel.WRITE)).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setId(10L);
            return savedPost;
        });

        // Act
        Post result = postService.createPost(title, content, forumId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(content, result.getContent());
        assertEquals(forumId, result.getForum().getId());
        assertEquals(userId, result.getUser().getId());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void testCreatePost_NoWriteAccess() {
        // Arrange
        String title = "New Post";
        String content = "New post content";
        Long forumId = 1L;
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(testUser);
        when(forumService.getForumById(forumId)).thenReturn(testForum);
        when(forumService.hasForumAccess(forumId, userId, AccessLevel.WRITE)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            postService.createPost(title, content, forumId, userId);
        });
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void testCreatePost_EmptyTitle() {
        // Arrange
        String title = "";
        String content = "New post content";
        Long forumId = 1L;
        Long userId = 1L;

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            postService.createPost(title, content, forumId, userId);
        });
    }

    @Test
    void testCreatePost_EmptyContent() {
        // Arrange
        String title = "New Post";
        String content = "";
        Long forumId = 1L;
        Long userId = 1L;

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            postService.createPost(title, content, forumId, userId);
        });
    }

    // Get Post Tests

    @Test
    void testGetPostById_Success() {
        // Arrange
        Long postId = 1L;
        Long userId = 1L;

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.READ)).thenReturn(true);

        // Act
        Post result = postService.getPostById(postId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(postId, result.getId());
    }

    @Test
    void testGetPostById_NotFound() {
        // Arrange
        Long postId = 999L;
        Long userId = 1L;

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            postService.getPostById(postId, userId);
        });
    }

    @Test
    void testGetPostById_NoReadAccess() {
        // Arrange
        Long postId = 1L;
        Long userId = 2L;

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.READ)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            postService.getPostById(postId, userId);
        });
    }

    // Update Post Tests

    @Test
    void testUpdatePost_AsAuthor() {
        // Arrange
        Long postId = 1L;
        String newTitle = "Updated Post";
        String newContent = "Updated content";
        Long userId = 1L; // Same as author

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Post result = postService.updatePost(postId, newTitle, newContent, userId);

        // Assert
        assertNotNull(result);
        assertEquals(newTitle, result.getTitle());
        assertEquals(newContent, result.getContent());
        verify(postRepository).save(testPost);
    }

    @Test
    void testUpdatePost_AsAdmin() {
        // Arrange
        Long postId = 1L;
        String newTitle = "Updated Post";
        String newContent = "Updated content";
        Long userId = 2L; // Different from author

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.ADMIN)).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Post result = postService.updatePost(postId, newTitle, newContent, userId);

        // Assert
        assertNotNull(result);
        assertEquals(newTitle, result.getTitle());
        assertEquals(newContent, result.getContent());
        verify(postRepository).save(testPost);
    }

    @Test
    void testUpdatePost_NotAuthorizedNotAdmin() {
        // Arrange
        Long postId = 1L;
        String newTitle = "Updated Post";
        String newContent = "Updated content";
        Long userId = 2L; // Different from author

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            postService.updatePost(postId, newTitle, newContent, userId);
        });
        verify(postRepository, never()).save(any(Post.class));
    }

    // Delete Post Tests

    @Test
    void testDeletePost_AsAuthor() {
        // Arrange
        Long postId = 1L;
        Long userId = 1L; // Same as author

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(contentRepository.findByPostId(postId)).thenReturn(Collections.emptyList());
        doNothing().when(contentRepository).deleteByPostId(postId);
        doNothing().when(postRepository).delete(testPost);

        // Act
        postService.deletePost(postId, userId);

        // Assert
        verify(contentRepository).deleteByPostId(postId);
        verify(postRepository).delete(testPost);
    }

    @Test
    void testDeletePost_AsAdmin() {
        // Arrange
        Long postId = 1L;
        Long userId = 2L; // Different from author

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.ADMIN)).thenReturn(true);
        when(contentRepository.findByPostId(postId)).thenReturn(Collections.emptyList());
        doNothing().when(contentRepository).deleteByPostId(postId);
        doNothing().when(postRepository).delete(testPost);

        // Act
        postService.deletePost(postId, userId);

        // Assert
        verify(contentRepository).deleteByPostId(postId);
        verify(postRepository).delete(testPost);
    }

    @Test
    void testDeletePost_NotAuthorizedNotAdmin() {
        // Arrange
        Long postId = 1L;
        Long userId = 2L; // Different from author

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            postService.deletePost(postId, userId);
        });
        verify(postRepository, never()).delete(any(Post.class));
    }

    // Post Content Tests

    @Test
    void testAddPostContent_Success() {
        // Arrange
        Long postId = 1L;
        Long userId = 1L; // Post author
        String description = "Test image";
        ContentType contentType = ContentType.IMAGE;
        boolean storeInDb = false;

        when(postService.getPostById(postId, userId)).thenReturn(testPost);
        when(contentRepository.save(any(Content.class))).thenAnswer(invocation -> {
            Content savedContent = invocation.getArgument(0);
            savedContent.setId(10L);
            return savedContent;
        });

        // Act
        Content result = postService.addPostContent(postId, testFile, description, contentType, storeInDb, userId);

        // Assert
        assertNotNull(result);
        assertEquals(description, result.getDescription());
        assertEquals(contentType, result.getContentType());
        assertEquals(false, result.isStoredInDb());
        verify(contentRepository).save(any(Content.class));
    }

    @Test
    void testGetPostContent_Success() {
        // Arrange
        Long postId = 1L;
        Long userId = 1L;
        List<Content> contents = Collections.singletonList(testContent);

        when(postService.getPostById(postId, userId)).thenReturn(testPost);
        when(contentRepository.findByPostId(postId)).thenReturn(contents);

        // Act
        List<Content> result = postService.getPostContent(postId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testContent.getId(), result.get(0).getId());
    }

    @Test
    void testDeletePostContent_Success() {
        // Arrange
        Long contentId = 1L;
        Long userId = 1L; // Post author

        when(contentRepository.findById(contentId)).thenReturn(Optional.of(testContent));
        doNothing().when(contentRepository).delete(testContent);

        // Act
        postService.deletePostContent(contentId, userId);

        // Assert
        verify(contentRepository).delete(testContent);
    }

    @Test
    void testDeletePostContent_NotAuthorizedNotAdmin() {
        // Arrange
        Long contentId = 1L;
        Long userId = 2L; // Not post author

        when(contentRepository.findById(contentId)).thenReturn(Optional.of(testContent));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            postService.deletePostContent(contentId, userId);
        });
        verify(contentRepository, never()).delete(any(Content.class));
    }

    // Pagination and Search Tests

    @Test
    void testGetPostsByForum_Success() {
        // Arrange
        Long forumId = 1L;
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        List<Post> posts = Arrays.asList(testPost, Post.builder().id(2L).build());
        Page<Post> page = new PageImpl<>(posts);

        when(forumService.hasForumAccess(forumId, userId, AccessLevel.READ)).thenReturn(true);
        when(postRepository.findByForumId(forumId, pageable)).thenReturn(page);

        // Act
        Page<Post> result = postService.getPostsByForum(forumId, userId, pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(page, result);
    }

    @Test
    void testGetPostsByForum_NoAccess() {
        // Arrange
        Long forumId = 1L;
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();

        when(forumService.hasForumAccess(forumId, userId, AccessLevel.READ)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            postService.getPostsByForum(forumId, userId, pageable);
        });
    }

    @Test
    void testSearchPosts_Success() {
        // Arrange
        String searchTerm = "test";
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        List<Post> posts = Arrays.asList(testPost, Post.builder().id(2L).build());
        Page<Post> page = new PageImpl<>(posts);

        when(postRepository.searchByTitleOrContent(searchTerm, pageable)).thenReturn(page);
        when(forumService.hasForumAccess(anyLong(), eq(userId), eq(AccessLevel.READ))).thenReturn(true);

        // Act
        Page<Post> result = postService.searchPosts(searchTerm, userId, pageable);

        // Assert
        assertEquals(2, result.getContent().size());
    }

    @Test
    void testSearchPostsInForum_Success() {
        // Arrange
        Long forumId = 1L;
        String searchTerm = "test";
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        List<Post> posts = Arrays.asList(testPost, Post.builder().id(2L).build());
        Page<Post> page = new PageImpl<>(posts);

        when(forumService.hasForumAccess(forumId, userId, AccessLevel.READ)).thenReturn(true);
        when(postRepository.searchByForumAndTitleOrContent(forumId, searchTerm, pageable)).thenReturn(page);

        // Act
        Page<Post> result = postService.searchPostsInForum(forumId, searchTerm, userId, pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(page, result);
    }
    
    @Test
    void testSearchPostsInForum_NoAccess() {
        // Arrange
        Long forumId = 1L;
        String searchTerm = "test";
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();

        when(forumService.hasForumAccess(forumId, userId, AccessLevel.READ)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            postService.searchPostsInForum(forumId, searchTerm, userId, pageable);
        });
    }
}
