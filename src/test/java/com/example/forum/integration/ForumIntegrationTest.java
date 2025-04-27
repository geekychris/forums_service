package com.example.forum.integration;

import com.example.forum.model.AccessLevel;
import com.example.forum.model.Comment;
import com.example.forum.model.Content;
import com.example.forum.model.ContentType;
import com.example.forum.model.Forum;
import com.example.forum.model.Post;
import com.example.forum.model.Role;
import com.example.forum.model.User;
import com.example.forum.repository.ForumRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.CommentService;
import com.example.forum.service.ForumService;
import com.example.forum.service.PostService;
import com.example.forum.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the forum application, testing the interaction
 * between different components: forums, posts, comments, and content.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ForumIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ForumRepository forumRepository;

    private User adminUser;
    private User regularUser;
    private User readerUser;
    private Forum testForum;
    private Forum subForum;

    @BeforeEach
    void setUp() {
        // Create users with different roles
        adminUser = userService.registerUser("admin", "password123", "admin@example.com", "Admin User", Role.ADMIN);
        regularUser = userService.registerUser("user", "password123", "user@example.com", "Regular User", Role.USER);
        readerUser = userService.registerUser("reader", "password123", "reader@example.com", "Reader User", Role.USER);

        // Create a test forum with the admin as creator
        testForum = forumService.createForum("Test Forum", "This is a test forum", adminUser.getId());

        // Create a subforum
        subForum = forumService.createSubforum("Test Subforum", "This is a test subforum", testForum.getId(), adminUser.getId());

        // Grant access to regular user (WRITE) and reader user (READ)
        forumService.grantForumAccess(testForum.getId(), regularUser.getId(), AccessLevel.WRITE, adminUser.getId());
        forumService.grantForumAccess(testForum.getId(), readerUser.getId(), AccessLevel.READ, adminUser.getId());

        // Also grant subforum access
        forumService.grantForumAccess(subForum.getId(), regularUser.getId(), AccessLevel.WRITE, adminUser.getId());
        forumService.grantForumAccess(subForum.getId(), readerUser.getId(), AccessLevel.READ, adminUser.getId());
    }

    @AfterEach
    void tearDown() {
        // Explicit cleanup if needed (though @Transactional should handle most cleanup)
    }

    @Test
    void testForumCreationAndHierarchy() {
        // Create a new parent forum
        Forum parentForum = forumService.createForum("Parent Forum", "Parent forum description", adminUser.getId());
        assertNotNull(parentForum.getId());
        assertEquals("Parent Forum", parentForum.getName());

        // Create a child forum
        Forum childForum = forumService.createSubforum("Child Forum", "Child forum description", parentForum.getId(), adminUser.getId());
        assertNotNull(childForum.getId());
        assertEquals("Child Forum", childForum.getName());
        assertEquals(parentForum.getId(), childForum.getParentForum().getId());

        // Create a grandchild forum (nested further)
        Forum grandchildForum = forumService.createSubforum("Grandchild Forum", "Grandchild forum description", childForum.getId(), adminUser.getId());
        assertNotNull(grandchildForum.getId());
        assertEquals(childForum.getId(), grandchildForum.getParentForum().getId());

        // Verify forum hierarchy
        List<Forum> rootForums = forumService.getRootForums();
        assertTrue(rootForums.stream().anyMatch(f -> f.getId().equals(parentForum.getId())));
        assertTrue(rootForums.stream().anyMatch(f -> f.getId().equals(testForum.getId())));

        List<Forum> childForums = forumService.getSubforums(parentForum.getId());
        assertEquals(1, childForums.size());
        assertEquals(childForum.getId(), childForums.get(0).getId());

        List<Forum> grandchildForums = forumService.getSubforums(childForum.getId());
        assertEquals(1, grandchildForums.size());
        assertEquals(grandchildForum.getId(), grandchildForums.get(0).getId());
    }

    @Test
    void testForumAccessControl() {
        // Create a restricted forum
        Forum restrictedForum = forumService.createForum("Restricted Forum", "Only admin access", adminUser.getId());

        // Verify admin has access
        assertTrue(forumService.hasForumAccess(restrictedForum.getId(), adminUser.getId(), AccessLevel.ADMIN));
        assertTrue(forumService.hasForumAccess(restrictedForum.getId(), adminUser.getId(), AccessLevel.WRITE));
        assertTrue(forumService.hasForumAccess(restrictedForum.getId(), adminUser.getId(), AccessLevel.READ));

        // Verify regular user has no access initially
        assertFalse(forumService.hasForumAccess(restrictedForum.getId(), regularUser.getId(), AccessLevel.READ));

        // Grant READ access to regular user
        boolean accessGranted = forumService.grantForumAccess(
                restrictedForum.getId(), regularUser.getId(), AccessLevel.READ, adminUser.getId());
        assertTrue(accessGranted);

        // Verify regular user now has READ access but not WRITE or ADMIN
        assertTrue(forumService.hasForumAccess(restrictedForum.getId(), regularUser.getId(), AccessLevel.READ));
        assertFalse(forumService.hasForumAccess(restrictedForum.getId(), regularUser.getId(), AccessLevel.WRITE));
        assertFalse(forumService.hasForumAccess(restrictedForum.getId(), regularUser.getId(), AccessLevel.ADMIN));

        // Upgrade to WRITE access
        boolean accessUpdated = forumService.updateForumAccess(
                restrictedForum.getId(), regularUser.getId(), AccessLevel.WRITE, adminUser.getId());
        assertTrue(accessUpdated);

        // Verify regular user now has WRITE and READ access
        assertTrue(forumService.hasForumAccess(restrictedForum.getId(), regularUser.getId(), AccessLevel.WRITE));
        assertTrue(forumService.hasForumAccess(restrictedForum.getId(), regularUser.getId(), AccessLevel.READ));

        // Revoke access
        boolean accessRevoked = forumService.revokeForumAccess(
                restrictedForum.getId(), regularUser.getId(), adminUser.getId());
        assertTrue(accessRevoked);

        // Verify regular user no longer has any access
        assertFalse(forumService.hasForumAccess(restrictedForum.getId(), regularUser.getId(), AccessLevel.READ));
    }

    @Test
    void testPostCreationAndManagement() {
        // Create a post as a regular user (who has WRITE access)
        Post post = postService.createPost(
                "Test Post Title",
                "This is the content of the test post.",
                testForum.getId(),
                regularUser.getId());

        assertNotNull(post.getId());
        assertEquals("Test Post Title", post.getTitle());
        assertEquals(regularUser.getId(), post.getUser().getId());
        assertEquals(testForum.getId(), post.getForum().getId());

        // Retrieve the post
        Post retrievedPost = postService.getPostById(post.getId(), regularUser.getId());
        assertEquals(post.getId(), retrievedPost.getId());

        // Update the post
        Post updatedPost = postService.updatePost(
                post.getId(),
                "Updated Post Title",
                "This is the updated content.",
                regularUser.getId());

        assertEquals("Updated Post Title", updatedPost.getTitle());
        assertEquals("This is the updated content.", updatedPost.getContent());

        // Get posts by forum
        Page<Post> forumPosts = postService.getPostsByForum(
                testForum.getId(),
                regularUser.getId(),
                PageRequest.of(0, 10));

        assertTrue(forumPosts.getContent().stream().anyMatch(p -> p.getId().equals(post.getId())));

        // Get posts by user
        Page<Post> userPosts = postService.getPostsByUser(
                regularUser.getId(),
                regularUser.getId(),
                PageRequest.of(0, 10));

        assertTrue(userPosts.getContent().stream().anyMatch(p -> p.getId().equals(post.getId())));

        // Delete the post
        postService.deletePost(post.getId(), regularUser.getId());

        // Verify post is deleted
        assertThrows(com.example.forum.exception.ResourceNotFoundException.class, () -> {
            postService.getPostById(post.getId(), regularUser.getId());
        });
    }

    @Test
    void testCommentThreadManagement() {
        // Create a post first
        Post post = postService.createPost(
                "Post for Comments",
                "This post will have comments.",
                testForum.getId(),
                regularUser.getId());

        // Add top-level comments
        Comment comment1 = commentService.createComment(
                post.getId(),
                "First comment on the post",
                regularUser.getId());

        Comment comment2 = commentService.createComment(
                post.getId(),
                "Second comment on the post",
                adminUser.getId());

        // Add nested replies
        Comment reply1 = commentService.createReply(
                comment1.getId(),
                "Reply to first comment",
                adminUser.getId());

        Comment replyToReply = commentService.createReply(
                reply1.getId(),
                "Reply to the reply",
                regularUser.getId());

        // Retrieve comments
        Page<Comment> topLevelComments = commentService.getCommentsByPost(
                post.getId(),
                regularUser.getId(),
                PageRequest.of(0, 10));

        assertEquals(2, topLevelComments.getContent().size());

        // Retrieve replies
        Page<Comment> replies = commentService.getRepliesByComment(
                comment1.getId(),
                regularUser.getId(),
                PageRequest.of(0, 10));

        assertEquals(1, replies.getContent().size());
        assertEquals(reply1.getId(), replies.getContent().get(0).getId());

        // Retrieve nested replies
        Page<Comment> nestedReplies = commentService.getRepliesByComment(
                reply1.getId(),
                regularUser.getId(),
                PageRequest.of(0, 10));

        assertEquals(1, nestedReplies.getContent().size());
        assertEquals(replyToReply.getId(), nestedReplies.getContent().get(0).getId());

        // Edit a comment
        Comment editedComment = commentService.updateComment(
                comment1.getId(),
                "Edited first comment",
                regularUser.getId());

        assertEquals("Edited first comment", editedComment.getContent());

        // Delete a comment with replies (should cascade delete)
        commentService.deleteComment(comment1.getId(), regularUser.getId());

        // Verify the comment and its replies are deleted
        assertThrows(com.example.forum.exception.ResourceNotFoundException.class, () -> {
            commentService.getCommentById(comment1.getId(), regularUser.getId());
        });

        assertThrows(com.example.forum.exception.ResourceNotFoundException.class, () -> {
            commentService.getCommentById(reply1.getId(), regularUser.getId());
        });

        assertThrows(com.example.forum.exception.ResourceNotFoundException.class, () -> {
            commentService.getCommentById(replyToReply.getId(), regularUser.getId());
        });
    }

    @Test
    void testContentManagement() throws Exception {
        // Create a post
        Post post = postService.createPost(
                "Post with Content",
                "This post will have attached content.",
                testForum.getId(),
                regularUser.getId());

        // Create a file to attach
        MultipartFile imageFile = new MockMultipartFile(
                "testImage.jpg",
                "testImage.jpg",
                "image/jpeg",
                "test image data".getBytes());

        // Attach content to the post
        Content postContent = postService.addPostContent(
                post.getId(),
                imageFile,
                "Test image description",
                ContentType.IMAGE,
                true, // store in DB
                regularUser.getId());

        assertNotNull(postContent.getId());
        assertEquals("testImage.jpg", postContent.getFilename());
        assertEquals(ContentType.IMAGE, postContent.getContentType());
        assertTrue(postContent.isStoredInDb());

        // Retrieve post content
        List<Content> postContents = postService.getPostContent(post.getId(), regularUser.getId());
        assertEquals(1, postContents.size());

        // Create a comment
        Comment comment = commentService.createComment(
                post.getId(),
                "Comment with content",
                regularUser.getId());

        // Attach content to the comment
        MultipartFile docFile = new MockMultipartFile(
                "testDoc.pdf",
                "testDoc.pdf",
                "application/pdf",
                "test document data".getBytes());

        Content commentContent = commentService.addCommentContent(
                comment.getId(),
                docFile,
                "Test document description",
                ContentType.DOCUMENT,
                true, // store in DB
                regularUser.getId());

        // Retrieve comment content
        List<Content> commentContents = commentService.getCommentContent(comment.getId(), regularUser.getId());
        assertEquals(1, commentContents.size());
        assertEquals(commentContent.getId(), commentContents.get(0).getId());

        // Delete post content
        postService.deletePostContent(postContent.getId(), regularUser.getId());

        // Verify post content is deleted
        List<Content> updatedPostContents = postService.getPostContent(post.getId(), regularUser.getId());
        assertEquals(0, updatedPostContents.size());

        // Delete comment content
        commentService.deleteCommentContent(commentContent.getId(), regularUser.getId());

        // Verify comment content is deleted
        List<Content> updatedCommentContents = commentService.getCommentContent(comment.getId(), regularUser.getId());
        assertEquals(0, updatedCommentContents.size());
    }

    @Test
    void testSearchFunctionality() {
        // Create forums with searchable names
        Forum techForum = forumService.createForum("Technology Forum", "Forum about technology", adminUser.getId());
        Forum scienceForum = forumService.createForum("Science Forum", "Forum about science", adminUser.getId());
        
        // Grant WRITE access for creating comments and search visibility
        forumService.grantForumAccess(techForum.getId(), regularUser.getId(), AccessLevel.WRITE, adminUser.getId());
        forumService.grantForumAccess(scienceForum.getId(), regularUser.getId(), AccessLevel.WRITE, adminUser.getId());

        // Create posts with searchable content
        Post techPost1 = postService.createPost(
                "Machine Learning",
                "Discussion about ML algorithms",
                techForum.getId(),
                adminUser.getId());
                
        Post techPost2 = postService.createPost(
                "Cloud Computing",
                "AWS vs Azure discussion",
                techForum.getId(),
                adminUser.getId());
                
        Post sciencePost = postService.createPost(
                "Quantum Physics",
                "Discussion about quantum mechanics",
                scienceForum.getId(),
                adminUser.getId());

        // Create comments with searchable content
        Comment mlComment = commentService.createComment(
                techPost1.getId(),
                "I think neural networks are the future of ML",
                regularUser.getId());
                
        Comment quantumComment = commentService.createComment(
                sciencePost.getId(),
                "Quantum computing will revolutionize technology",
                regularUser.getId());

        // Test forum search
        List<Forum> techForums = forumService.searchForums("Technology");
        assertEquals(1, techForums.size());
        assertEquals("Technology Forum", techForums.get(0).getName());

        // Test post search (across all forums)
        Page<Post> quantumPosts = postService.searchPosts(
                "quantum", 
                regularUser.getId(), 
                PageRequest.of(0, 10));
        assertEquals(1, quantumPosts.getTotalElements());
        assertEquals("Quantum Physics", quantumPosts.getContent().get(0).getTitle());

        // Test post search (in specific forum)
        Page<Post> cloudPosts = postService.searchPostsInForum(
                techForum.getId(),
                "cloud",
                regularUser.getId(),
                PageRequest.of(0, 10));
        assertEquals(1, cloudPosts.getTotalElements());
        assertEquals("Cloud Computing", cloudPosts.getContent().get(0).getTitle());

        // Test comment search
        Page<Comment> neuralComments = commentService.searchComments(
                "neural",
                regularUser.getId(),
                PageRequest.of(0, 10));
        assertEquals(1, neuralComments.getTotalElements());
        assertTrue(neuralComments.getContent().get(0).getContent().contains("neural networks"));
    }

    @Test
    void testForumMovement() {
        // Create a hierarchy of forums
        Forum parentForum1 = forumService.createForum("Parent Forum 1", "First parent forum", adminUser.getId());
        Forum parentForum2 = forumService.createForum("Parent Forum 2", "Second parent forum", adminUser.getId());
        
        Forum childForum = forumService.createSubforum("Child Forum", "Child forum", parentForum1.getId(), adminUser.getId());
        
        // Verify initial hierarchy
        List<Forum> parent1Children = forumService.getSubforums(parentForum1.getId());
        assertEquals(1, parent1Children.size());
        assertEquals(childForum.getId(), parent1Children.get(0).getId());
        
        List<Forum> parent2Children = forumService.getSubforums(parentForum2.getId());
        assertEquals(0, parent2Children.size());
        
        // Move child forum to a different parent
        Forum movedForum = forumService.moveForum(childForum.getId(), parentForum2.getId(), adminUser.getId());
        
        // Verify new hierarchy
        assertEquals(parentForum2.getId(), movedForum.getParentForum().getId());
        
        parent1Children = forumService.getSubforums(parentForum1.getId());
        assertEquals(0, parent1Children.size());
        
        parent2Children = forumService.getSubforums(parentForum2.getId());
        assertEquals(1, parent2Children.size());
        assertEquals(childForum.getId(), parent2Children.get(0).getId());
        
        // Move child forum to root level (no parent)
        movedForum = forumService.moveForum(childForum.getId(), null, adminUser.getId());
        
        // Verify forum is now at root level
        assertNull(movedForum.getParentForum());
        
        List<Forum> rootForums = forumService.getRootForums();
        assertTrue(rootForums.stream().anyMatch(f -> f.getId().equals(childForum.getId())));
        
        parent2Children = forumService.getSubforums(parentForum2.getId());
        assertEquals(0, parent2Children.size());
    }
}
