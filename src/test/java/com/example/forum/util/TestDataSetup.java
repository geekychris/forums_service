package com.example.forum.util;

import com.example.forum.model.*;
import com.example.forum.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Utility class for setting up test data
 */
@Component
public class TestDataSetup {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ForumRepository forumRepository;

    @Autowired
    private ForumAccessRepository forumAccessRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Test data storage for cleanup
    private final Map<String, Long> testUsers = new HashMap<>();
    private final Map<String, Long> testForums = new HashMap<>();
    private final Map<String, Long> testPosts = new HashMap<>();
    private final Map<String, Long> testComments = new HashMap<>();

    /**
     * Creates a test user with specified role
     */
    @Transactional
    public User createTestUser(String username, String password, String email, Role role) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .displayName(username + " Display Name")
                .role(role)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .posts(new HashSet<>())
                .comments(new HashSet<>())
                .forumAccesses(new HashSet<>())
                .build();
        
        User savedUser = userRepository.save(user);
        testUsers.put(username, savedUser.getId());
        return savedUser;
    }

    /**
     * Creates a test forum
     */
    @Transactional
    public Forum createTestForum(String name, String description, Forum parentForum, User creator) {
        Forum forum = Forum.builder()
                .name(name)
                .description(description)
                .parentForum(parentForum)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .posts(new HashSet<>())
                .subForums(new HashSet<>())
                .build();
        
        Forum savedForum = forumRepository.save(forum);
        testForums.put(name, savedForum.getId());
        
        // Create admin access for the creator
        if (creator != null) {
            ForumAccess access = ForumAccess.builder()
                    .user(creator)
                    .forum(savedForum)
                    .accessLevel(AccessLevel.ADMIN)
                    .grantedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            forumAccessRepository.save(access);
        }
        
        return savedForum;
    }

    /**
     * Grants access to a forum for a user
     */
    @Transactional
    public ForumAccess grantForumAccess(User user, Forum forum, AccessLevel accessLevel) {
        ForumAccess access = ForumAccess.builder()
                .user(user)
                .forum(forum)
                .accessLevel(accessLevel)
                .grantedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return forumAccessRepository.save(access);
    }

    /**
     * Creates a test post
     */
    @Transactional
    public Post createTestPost(String title, String content, Forum forum, User author) {
        Post post = Post.builder()
                .title(title)
                .content(content)
                .forum(forum)
                .user(author)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .comments(new HashSet<>())
                .contents(new HashSet<>())
                .build();
        
        Post savedPost = postRepository.save(post);
        testPosts.put(title, savedPost.getId());
        return savedPost;
    }

    /**
     * Creates a test comment
     */
    @Transactional
    public Comment createTestComment(String content, Post post, User author, Comment parentComment) {
        Comment comment = Comment.builder()
                .content(content)
                .post(post)
                .user(author)
                .parentComment(parentComment)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .replies(new HashSet<>())
                .contents(new HashSet<>())
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        testComments.put(content, savedComment.getId());
        return savedComment;
    }

    /**
     * Creates common test fixture data
     */
    @Transactional
    public void createCommonTestData() {
        // Create users
        User admin = createTestUser("admin", "password", "admin@example.com", Role.ADMIN);
        User moderator = createTestUser("moderator", "password", "mod@example.com", Role.MODERATOR);
        User regularUser = createTestUser("user", "password", "user@example.com", Role.USER);
        
        // Create forums
        Forum rootForum = createTestForum("Root Forum", "Main forum", null, admin);
        Forum subForum1 = createTestForum("Sub Forum 1", "First sub-forum", rootForum, admin);
        Forum subForum2 = createTestForum("Sub Forum 2", "Second sub-forum", rootForum, admin);
        
        // Grant access
        grantForumAccess(moderator, rootForum, AccessLevel.ADMIN);
        grantForumAccess(moderator, subForum1, AccessLevel.ADMIN);
        grantForumAccess(moderator, subForum2, AccessLevel.ADMIN);
        
        grantForumAccess(regularUser, rootForum, AccessLevel.READ);
        grantForumAccess(regularUser, subForum1, AccessLevel.WRITE);
        grantForumAccess(regularUser, subForum2, AccessLevel.READ);
        
        // Create posts
        Post post1 = createTestPost("First Post", "Content of first post", subForum1, admin);
        Post post2 = createTestPost("Second Post", "Content of second post", subForum1, moderator);
        Post post3 = createTestPost("Third Post", "Content of third post", subForum2, regularUser);
        
        // Create comments
        Comment comment1 = createTestComment("First comment", post1, moderator, null);
        Comment comment2 = createTestComment("Reply to first comment", post1, regularUser, comment1);
        Comment comment3 = createTestComment("Another comment", post2, admin, null);
    }

    /**
     * Cleans up test data
     */
    @Transactional
    public void cleanupTestData() {
        // Clean up in reverse order of dependencies
        testComments.forEach((content, id) -> commentRepository.deleteById(id));
        testPosts.forEach((title, id) -> postRepository.deleteById(id));
        
        // Clean up forum access before forums
        testForums.forEach((name, id) -> {
            forumAccessRepository.deleteByForumId(id);
            forumRepository.deleteById(id);
        });
        
        testUsers.forEach((username, id) -> userRepository.deleteById(id));
        
        // Clear maps
        testComments.clear();
        testPosts.clear();
        testForums.clear();
        testUsers.clear();
    }

    /**
     * Get user ID by test username
     */
    public Long getUserId(String username) {
        return testUsers.get(username);
    }

    /**
     * Get forum ID by test forum name
     */
    public Long getForumId(String forumName) {
        return testForums.get(forumName);
    }

    /**
     * Get post ID by test post title
     */
    public Long getPostId(String postTitle) {
        return testPosts.get(postTitle);
    }

    /**
     * Get comment ID by test comment content
     */
    public Long getCommentId(String commentContent) {
        return testComments.get(commentContent);
    }
}

