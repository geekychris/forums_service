package com.example.forum.service;

import com.example.forum.exception.AccessDeniedException;
import com.example.forum.exception.BadRequestException;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.model.AccessLevel;
import com.example.forum.model.Comment;
import com.example.forum.model.Content;
import com.example.forum.model.ContentType;
import com.example.forum.model.Forum;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.ContentRepository;
import com.example.forum.service.impl.CommentServiceImpl;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private PostService postService;

    @Mock
    private ForumService forumService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User testUser;
    private User testAdmin;
    private Forum testForum;
    private Post testPost;
    private Comment testComment;
    private Comment testReply;
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

        testAdmin = User.builder()
                .id(2L)
                .username("admin")
                .displayName("Admin User")
                .email("admin@example.com")
                .active(true)
                .build();

        testForum = Forum.builder()
                .id(1L)
                .name("Test Forum")
                .description("Test forum description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
                .build();

        testComment = Comment.builder()
                .id(1L)
                .content("Test comment content")
                .user(testUser)
                .post(testPost)
                .parentComment(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .replies(new HashSet<>())
                .contents(new HashSet<>())
                .build();

        testReply = Comment.builder()
                .id(2L)
                .content("Test reply content")
                .user(testUser)
                .post(testPost)
                .parentComment(testComment)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .replies(new HashSet<>())
                .contents(new HashSet<>())
                .build();

        testContent = Content.builder()
                .id(1L)
                .filename("testimage.jpg")
                .contentPath("/path/to/file")
                .contentType(ContentType.IMAGE)
                .description("Test image")
                .storedInDb(false)
                .comment(testComment)
                .createdAt(LocalDateTime.now())
                .build();

        testFile = new MockMultipartFile(
                "file",
                "testimage.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    // Create Comment Tests

    @Test
    void testCreateComment_Success() {
        // Arrange
        Long postId = 1L;
        String content = "New comment";
        Long userId = 1L;

        when(postService.getPostById(postId, userId)).thenReturn(testPost);
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.WRITE)).thenReturn(true);
        when(userService.getUserById(userId)).thenReturn(testUser);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            savedComment.setId(10L);
            return savedComment;
        });

        // Act
        Comment result = commentService.createComment(postId, content, userId);

        // Assert
        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(postId, result.getPost().getId());
        assertEquals(userId, result.getUser().getId());
        assertNull(result.getParentComment());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void testCreateComment_NoWriteAccess() {
        // Arrange
        Long postId = 1L;
        String content = "New comment";
        Long userId = 1L;

        when(postService.getPostById(postId, userId)).thenReturn(testPost);
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.WRITE)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            commentService.createComment(postId, content, userId);
        });
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testCreateComment_EmptyContent() {
        // Arrange
        Long postId = 1L;
        String content = "";
        Long userId = 1L;

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            commentService.createComment(postId, content, userId);
        });
    }

    // Create Reply Tests

    @Test
    void testCreateReply_Success() {
        // Arrange
        Long parentCommentId = 1L;
        String content = "New reply";
        Long userId = 1L;

        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.of(testComment));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.WRITE)).thenReturn(true);
        when(userService.getUserById(userId)).thenReturn(testUser);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            savedComment.setId(10L);
            return savedComment;
        });

        // Act
        Comment result = commentService.createReply(parentCommentId, content, userId);

        // Assert
        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(testPost.getId(), result.getPost().getId());
        assertEquals(userId, result.getUser().getId());
        assertEquals(parentCommentId, result.getParentComment().getId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void testCreateReply_NoWriteAccess() {
        // Arrange
        Long parentCommentId = 1L;
        String content = "New reply";
        Long userId = 1L;

        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.of(testComment));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.WRITE)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            commentService.createReply(parentCommentId, content, userId);
        });
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // Get Comment Tests

    @Test
    void testGetCommentById_Success() {
        // Arrange
        Long commentId = 1L;
        Long userId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.READ)).thenReturn(true);

        // Act
        Comment result = commentService.getCommentById(commentId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(commentId, result.getId());
    }

    @Test
    void testGetCommentById_NotFound() {
        // Arrange
        Long commentId = 999L;
        Long userId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getCommentById(commentId, userId);
        });
    }

    @Test
    void testGetCommentById_NoReadAccess() {
        // Arrange
        Long commentId = 1L;
        Long userId = 3L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.READ)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            commentService.getCommentById(commentId, userId);
        });
    }

    // Update Comment Tests

    @Test
    void testUpdateComment_AsAuthor() {
        // Arrange
        Long commentId = 1L;
        String newContent = "Updated comment";
        Long userId = 1L; // Same as author

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Comment result = commentService.updateComment(commentId, newContent, userId);

        // Assert
        assertNotNull(result);
        assertEquals(newContent, result.getContent());
        verify(commentRepository).save(testComment);
    }

    @Test
    void testUpdateComment_AsAdmin() {
        // Arrange
        Long commentId = 1L;
        String newContent = "Updated comment";
        Long userId = 2L; // Different from author

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.ADMIN)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Comment result = commentService.updateComment(commentId, newContent, userId);

        // Assert
        assertNotNull(result);
        assertEquals(newContent, result.getContent());
        verify(commentRepository).save(testComment);
    }

    @Test
    void testUpdateComment_NotAuthorizedNotAdmin() {
        // Arrange
        Long commentId = 1L;
        String newContent = "Updated comment";
        Long userId = 3L; // Different from author

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            commentService.updateComment(commentId, newContent, userId);
        });
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // Delete Comment Tests

    @Test
    void testDeleteComment_AsAuthor() {
        // Arrange
        Long commentId = 1L;
        Long userId = 1L; // Same as author

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.findByParentCommentId(commentId, Pageable.unpaged())).thenReturn(Page.empty());
        when(contentRepository.findByCommentId(commentId)).thenReturn(Collections.emptyList());
        doNothing().when(contentRepository).deleteByCommentId(commentId);
        doNothing().when(commentRepository).delete(testComment);

        // Act
        commentService.deleteComment(commentId, userId);

        // Assert
        verify(contentRepository).deleteByCommentId(commentId);
        verify(commentRepository).delete(testComment);
    }
    @Test
    void testDeleteComment_WithReplies() {
        // Arrange
        Long commentId = 1L;
        Long userId = 1L; // Same as author
        List<Comment> replies = Collections.singletonList(testReply);
        Page<Comment> repliesPage = new PageImpl<>(replies);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.findByParentCommentId(commentId, Pageable.unpaged())).thenReturn(repliesPage);
        
        // For the recursive call to delete the reply
        when(commentRepository.findById(testReply.getId())).thenReturn(Optional.of(testReply));
        when(commentRepository.findByParentCommentId(testReply.getId(), Pageable.unpaged())).thenReturn(Page.empty());
        when(contentRepository.findByCommentId(testReply.getId())).thenReturn(Collections.emptyList());
        doNothing().when(contentRepository).deleteByCommentId(testReply.getId());
        doNothing().when(commentRepository).delete(testReply);
        
        // For the original comment
        when(contentRepository.findByCommentId(commentId)).thenReturn(Collections.emptyList());
        doNothing().when(contentRepository).deleteByCommentId(commentId);
        doNothing().when(commentRepository).delete(testComment);

        // Act
        commentService.deleteComment(commentId, userId);

        // Assert
        verify(contentRepository).deleteByCommentId(commentId);
        verify(contentRepository).deleteByCommentId(testReply.getId());
        verify(commentRepository).delete(testComment);
        verify(commentRepository).delete(testReply);
    }

    @Test
    void testDeleteComment_AsPostAuthor() {
        // Arrange
        Long commentId = 1L;
        Long userId = 1L; // Same as post author
        testComment.setUser(testAdmin); // Comment made by different user

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.findByParentCommentId(commentId, Pageable.unpaged())).thenReturn(Page.empty());
        when(contentRepository.findByCommentId(commentId)).thenReturn(Collections.emptyList());
        doNothing().when(contentRepository).deleteByCommentId(commentId);
        doNothing().when(commentRepository).delete(testComment);

        // Act
        commentService.deleteComment(commentId, userId);

        // Assert
        verify(contentRepository).deleteByCommentId(commentId);
        verify(commentRepository).delete(testComment);
    }

    @Test
    void testDeleteComment_AsAdmin() {
        // Arrange
        Long commentId = 1L;
        Long userId = 2L; // Admin, different from author

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.ADMIN)).thenReturn(true);
        when(commentRepository.findByParentCommentId(commentId, Pageable.unpaged())).thenReturn(Page.empty());
        when(contentRepository.findByCommentId(commentId)).thenReturn(Collections.emptyList());
        doNothing().when(contentRepository).deleteByCommentId(commentId);
        doNothing().when(commentRepository).delete(testComment);

        // Act
        commentService.deleteComment(commentId, userId);

        // Assert
        verify(contentRepository).deleteByCommentId(commentId);
        verify(commentRepository).delete(testComment);
    }

    @Test
    void testDeleteComment_NotAuthorized() {
        // Arrange
        Long commentId = 1L;
        Long userId = 3L; // Not author, not post author, not admin

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            commentService.deleteComment(commentId, userId);
        });
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    // Comment Content Tests

    @Test
    void testAddCommentContent_Success() {
        // Arrange
        Long commentId = 1L;
        Long userId = 1L; // Comment author
        String description = "Test image";
        ContentType contentType = ContentType.IMAGE;
        boolean storeInDb = false;

        when(commentService.getCommentById(commentId, userId)).thenReturn(testComment);
        when(contentRepository.save(any(Content.class))).thenAnswer(invocation -> {
            Content savedContent = invocation.getArgument(0);
            savedContent.setId(10L);
            return savedContent;
        });

        // Act
        Content result = commentService.addCommentContent(commentId, testFile, description, contentType, storeInDb, userId);

        // Assert
        assertNotNull(result);
        assertEquals(description, result.getDescription());
        assertEquals(contentType, result.getContentType());
        assertEquals(false, result.isStoredInDb());
        verify(contentRepository).save(any(Content.class));
    }

    @Test
    void testGetCommentContent_Success() {
        // Arrange
        Long commentId = 1L;
        Long userId = 1L;
        List<Content> contents = Collections.singletonList(testContent);

        when(commentService.getCommentById(commentId, userId)).thenReturn(testComment);
        when(contentRepository.findByCommentId(commentId)).thenReturn(contents);

        // Act
        List<Content> result = commentService.getCommentContent(commentId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testContent.getId(), result.get(0).getId());
    }

    @Test
    void testDeleteCommentContent_Success() {
        // Arrange
        Long contentId = 1L;
        Long userId = 1L; // Comment author

        when(contentRepository.findById(contentId)).thenReturn(Optional.of(testContent));
        doNothing().when(contentRepository).delete(testContent);

        // Act
        commentService.deleteCommentContent(contentId, userId);

        // Assert
        verify(contentRepository).delete(testContent);
    }

    @Test
    void testDeleteCommentContent_NotAuthorizedNotAdmin() {
        // Arrange
        Long contentId = 1L;
        Long userId = 3L; // Not comment author

        when(contentRepository.findById(contentId)).thenReturn(Optional.of(testContent));
        when(forumService.hasForumAccess(testPost.getForum().getId(), userId, AccessLevel.ADMIN)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            commentService.deleteCommentContent(contentId, userId);
        });
        verify(contentRepository, never()).delete(any(Content.class));
    }

    // Pagination Tests

    @Test
    void testGetCommentsByPost_Success() {
        // Arrange
        Long postId = 1L;
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        List<Comment> comments = Collections.singletonList(testComment);
        Page<Comment> page = new PageImpl<>(comments);

        when(postService.getPostById(postId, userId)).thenReturn(testPost);
        when(commentRepository.findByPostIdAndParentCommentIsNull(postId, pageable)).thenReturn(page);

        // Act
        Page<Comment> result = commentService.getCommentsByPost(postId, userId, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(testComment.getId(), result.getContent().get(0).getId());
    }

    @Test
    void testGetRepliesByComment_Success() {
        // Arrange
        Long commentId = 1L;
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        List<Comment> replies = Collections.singletonList(testReply);
        Page<Comment> page = new PageImpl<>(replies);

        when(commentService.getCommentById(commentId, userId)).thenReturn(testComment);
        when(commentRepository.findByParentCommentId(commentId, pageable)).thenReturn(page);

        // Act
        Page<Comment> result = commentService.getRepliesByComment(commentId, userId, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(testReply.getId(), result.getContent().get(0).getId());
    }

    // Search Tests

    @Test
    void testSearchComments_Success() {
        // Arrange
        String searchTerm = "test";
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        List<Comment> comments = Arrays.asList(testComment, testReply);
        Page<Comment> page = new PageImpl<>(comments);

        when(commentRepository.searchByContent(searchTerm, pageable)).thenReturn(page);
        when(forumService.hasForumAccess(anyLong(), eq(userId), eq(AccessLevel.READ))).thenReturn(true);

        // Act
        Page<Comment> result = commentService.searchComments(searchTerm, userId, pageable);

        // Assert
        assertEquals(2, result.getContent().size());
    }

    @Test
    void testSearchComments_EmptyTerm() {
        // Arrange
        String searchTerm = "";
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            commentService.searchComments(searchTerm, userId, pageable);
        });
    }
}
