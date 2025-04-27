package com.example.forum.integration;

import com.example.forum.dto.auth.JwtResponse;
import com.example.forum.dto.auth.LoginRequest;
import com.example.forum.dto.auth.RegisterRequest;
import com.example.forum.dto.forum.CreateForumRequest;
import com.example.forum.dto.forum.ForumAccessRequest;
import com.example.forum.dto.post.CreatePostRequest;
import com.example.forum.model.AccessLevel;
import com.jayway.jsonpath.JsonPath;
import com.example.forum.model.ContentType;
import com.example.forum.model.Role;
import com.example.forum.model.User;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static java.util.Map.of;

import java.util.Map;

/**
 * API integration tests for the forum application using MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ForumApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private String readerToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register and authenticate users
        adminToken = registerAndLogin("admin", "admin@example.com", "password123", "Admin User", Role.ADMIN);
        userToken = registerAndLogin("regular", "regular@example.com", "password123", "Regular User", Role.USER);
        readerToken = registerAndLogin("reader", "reader@example.com", "password123", "Reader User", Role.USER);
    }

    private String registerAndLogin(String username, String email, String password, String displayName, Role role) throws Exception {
        // Register user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .email(email)
                .password(password)
                .displayName(displayName)
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Set the role directly (since we can't register as admin directly)
        if (role == Role.ADMIN) {
            User user = userRepository.findByUsername(username).orElseThrow();
            user.setRole(Role.ADMIN);
            userRepository.save(user);
        }

        // Login and get token
        LoginRequest loginRequest = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse jwtResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), JwtResponse.class);
        
        return jwtResponse.getAccessToken();
    }
    
    /**
     * Helper method to safely convert JsonPath result to Long
     * Handles the case where JsonPath returns Integer for numeric JSON values
     */
    private Long extractLongId(String json, String jsonPath) {
        Object idObj = JsonPath.read(json, jsonPath);
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        // Fallback to string conversion if needed
        return Long.valueOf(idObj.toString());
    }

    // Authentication Tests

    @Test
    void testAuthenticationFlow() throws Exception {
        // Test that we can get current user info with token
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.role", is("ADMIN")));

        // Test with invalid token
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized());

        // Test with no token
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // Forum Management Tests

    @Test
    void testForumCrudOperations() throws Exception {
        // Create forum
        CreateForumRequest createForumRequest = CreateForumRequest.builder()
                .name("Test API Forum")
                .description("Forum created through API")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/forums")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createForumRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Test API Forum")))
                .andReturn();

        // Extract forum ID
        // Extract forum ID
        String responseJson = createResult.getResponse().getContentAsString();
        Long forumId = extractLongId(responseJson, "$.id");
        // Get forum by ID
        mockMvc.perform(get("/api/forums/{id}", forumId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test API Forum")));

        // Update forum
        mockMvc.perform(put("/api/forums/{id}", forumId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Updated Forum Name",
                        "description", "Updated description"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Forum Name")));

        // Create subforum
        CreateForumRequest createSubforumRequest = CreateForumRequest.builder()
                .name("Test API Subforum")
                .description("Subforum created through API")
                .build();

        mockMvc.perform(post("/api/forums/{parentId}/subforums", forumId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createSubforumRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Test API Subforum")))
                .andExpect(jsonPath("$.parentForumId", is(forumId)));
        // Delete forum - this should fail because it has subforums
        mockMvc.perform(delete("/api/forums/{id}", forumId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());

        // Get the ID of the subforum to delete it first
        MvcResult subforumsResult = mockMvc.perform(get("/api/forums/{parentId}/subforums", forumId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        String subforumsJson = subforumsResult.getResponse().getContentAsString();
        Long subforumId = extractLongId(subforumsJson, "$[0].id");
        mockMvc.perform(delete("/api/forums/{id}", subforumId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Now delete the main forum
        mockMvc.perform(delete("/api/forums/{id}", forumId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Verify forum is gone
        mockMvc.perform(get("/api/forums/{id}", forumId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    /**
     * Test forum access management endpoints
     */
    @Test
    void testForumAccessManagement() throws Exception {
        // Create a test forum
        CreateForumRequest createForumRequest = CreateForumRequest.builder()
                .name("Access Test Forum")
                .description("Forum for testing access control")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/forums")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createForumRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Long forumId = extractLongId(responseJson, "$.id");
        // Verify reader can access forum initially (forums are accessible by default)
        mockMvc.perform(get("/api/forums/{id}", forumId)
                .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk());

        // Grant read access to reader
        ForumAccessRequest accessRequest = ForumAccessRequest.builder()
                .userId(extractLongId(mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + readerToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(), "$.id"))
                .accessLevel(AccessLevel.READ)
                .build();
        mockMvc.perform(post("/api/forums/{id}/access", forumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accessRequest)))
                .andExpect(status().isOk());

        // Verify reader can now access forum
        mockMvc.perform(get("/api/forums/{id}", forumId)
                .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Access Test Forum")));

        // Update access to write
        accessRequest.setAccessLevel(AccessLevel.WRITE);
        mockMvc.perform(put("/api/forums/{id}/access", forumId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accessRequest)))
                .andExpect(status().isOk());

        // Revoke access from reader
        mockMvc.perform(delete("/api/forums/{id}/access/{userId}", forumId, accessRequest.getUserId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Verify reader can't access forum again
        mockMvc.perform(get("/api/forums/{id}", forumId)
                .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Test post management endpoints
     */
    @Test
    void testPostManagement() throws Exception {
        // Create a test forum
        CreateForumRequest createForumRequest = CreateForumRequest.builder()
                .name("Post Test Forum")
                .description("Forum for testing posts")
                .build();

        MvcResult createForumResult = mockMvc.perform(post("/api/forums")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createForumRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        Long forumId = extractLongId(createForumResult.getResponse().getContentAsString(), "$.id");

        // Grant access to regular user
        ForumAccessRequest accessRequest = ForumAccessRequest.builder()
                .userId(extractLongId(mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(), "$.id"))
                .accessLevel(AccessLevel.WRITE)
                .build();
        mockMvc.perform(post("/api/forums/{id}/access", forumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accessRequest)))
                .andExpect(status().isOk());

        // Create a post
        CreatePostRequest createPostRequest = CreatePostRequest.builder()
                .title("Test API Post")
                .content("This is a test post created through the API")
                .forumId(forumId)
                .build();

        MvcResult createPostResult = mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Test API Post")))
                .andReturn();
        Long postId = extractLongId(createPostResult.getResponse().getContentAsString(), "$.id");

        // Get post by ID
        mockMvc.perform(get("/api/posts/{id}", postId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test API Post")));

        // Update post
        mockMvc.perform(put("/api/posts/{id}", postId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "title", "Updated Post Title",
                        "content", "This is updated content"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Post Title")));

        // Get posts by forum
        mockMvc.perform(get("/api/posts/by-forum/{forumId}", forumId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(postId)));

        // Delete post
        mockMvc.perform(delete("/api/posts/{id}", postId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // Verify post is deleted
        mockMvc.perform(get("/api/posts/{id}", postId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    /**
     * Test comment management endpoints
     */
    @Test
    void testCommentManagement() throws Exception {
        // Create a forum and post first
        CreateForumRequest createForumRequest = CreateForumRequest.builder()
                .name("Comment Test Forum")
                .description("Forum for testing comments")
                .build();
        Long forumId = extractLongId(mockMvc
                .perform(post("/api/forums")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createForumRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");


        // Grant access to regular user
        ForumAccessRequest accessRequest = ForumAccessRequest.builder()
                .userId(extractLongId(mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(), "$.id"))
                .accessLevel(AccessLevel.WRITE)
                .build();
                
        mockMvc.perform(post("/api/forums/{id}/access", forumId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accessRequest)))
                .andExpect(status().isOk());

        // Create a post
        CreatePostRequest createPostRequest = CreatePostRequest.builder()
                .title("Post for Comments")
                .content("This post will have comments")
                .forumId(forumId)
                .build();
        Long postId = extractLongId(mockMvc
                .perform(post("/api/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        // Create a comment
        Map<String, Object> createCommentRequest = Map.of(
                "postId", postId,
                "content", "This is a test comment"
        );

        MvcResult createCommentResult = mockMvc.perform(post("/api/comments")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCommentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", is("This is a test comment")))
                .andReturn();
        Long commentId = extractLongId(createCommentResult.getResponse().getContentAsString(), "$.id");

        // Create a reply to the comment
        Map<String, Object> createReplyRequest = Map.of(
                "parentCommentId", commentId,
                "content", "This is a reply to the comment"
        );

        MvcResult createReplyResult = mockMvc.perform(post("/api/comments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReplyRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", is("This is a reply to the comment")))
                .andExpect(jsonPath("$.parentCommentId", is(commentId)))
                .andReturn();
        Long replyId = extractLongId(createReplyResult.getResponse().getContentAsString(), "$.id");

        // Get comments by post
        mockMvc.perform(get("/api/comments/by-post/{postId}", postId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(commentId)));

        // Get replies by comment
        mockMvc.perform(get("/api/comments/{commentId}/replies", commentId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(replyId)));
        // Update comment
        Map<String, Object> updateCommentRequest = Map.of(
                "content", "This is an updated comment"
        );

        mockMvc.perform(put("/api/comments/{id}", commentId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("This is an updated comment")));

        // Test permission - reader user shouldn't be able to update the comment
        mockMvc.perform(put("/api/comments/{id}", commentId)
                .header("Authorization", "Bearer " + readerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCommentRequest)))
                .andExpect(status().isForbidden());

        // Delete reply first
        mockMvc.perform(delete("/api/comments/{id}", replyId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Verify reply is deleted
        mockMvc.perform(get("/api/comments/{id}", replyId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        // Delete the original comment
        mockMvc.perform(delete("/api/comments/{id}", commentId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // Verify comment is deleted
        mockMvc.perform(get("/api/comments/{id}", commentId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    /**
     * Test content management endpoints for posts and comments
     */
    @Test
    void testContentManagement() throws Exception {
        // Create a forum, post, and comment first
        CreateForumRequest createForumRequest = CreateForumRequest.builder()
                .name("Content Test Forum")
                .description("Forum for testing content uploads")
                .build();
        Long forumId = extractLongId(mockMvc
                .perform(post("/api/forums")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createForumRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        // Grant access to regular user
        ForumAccessRequest accessRequest = ForumAccessRequest.builder()
                .userId(extractLongId(mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(), "$.id"))
                .accessLevel(AccessLevel.WRITE)
                .build();
        mockMvc.perform(post("/api/forums/{id}/access", forumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accessRequest)))
                .andExpect(status().isOk());

        // Create a post
        CreatePostRequest createPostRequest = CreatePostRequest.builder()
                .title("Post for Content")
                .content("This post will have content attachments")
                .forumId(forumId)
                .build();
        Long postId = extractLongId(mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");

        // Create a comment
        Map<String, Object> createCommentRequest = Map.of(
                "postId", postId,
                "content", "This comment will have content attachments"
        );
        Long commentId = extractLongId(mockMvc.perform(post("/api/comments")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCommentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        // Create test file for upload
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image data".getBytes());

        // Upload content to post
        MvcResult postContentResult = mockMvc.perform(multipart("/api/posts/{id}/content", postId)
                .file(imageFile)
                .param("description", "Test post image")
                .param("contentType", ContentType.IMAGE.name())
                .param("storeInDb", "true")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename", is("test-image.jpg")))
                .andExpect(jsonPath("$.contentType", is("IMAGE")))
                .andReturn();
        Long postContentId = extractLongId(postContentResult.getResponse().getContentAsString(), "$.id");

        // Get post content
        mockMvc.perform(get("/api/posts/{id}/content", postId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(postContentId)));

        // Upload content to comment
        MockMultipartFile docFile = new MockMultipartFile(
                "file",
                "test-doc.pdf",
                "application/pdf",
                "test document data".getBytes());

        MvcResult commentContentResult = mockMvc.perform(multipart("/api/comments/{id}/content", commentId)
                .file(docFile)
                .param("description", "Test comment document")
                .param("contentType", ContentType.DOCUMENT.name())
                .param("storeInDb", "true")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename", is("test-doc.pdf")))
                .andExpect(jsonPath("$.contentType", is("DOCUMENT")))
                .andReturn();

        Long commentContentId = extractLongId(commentContentResult.getResponse().getContentAsString(), "$.id");

        // Get comment content
        mockMvc.perform(get("/api/comments/{id}/content", commentId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(commentContentId)));

        // Test content type validation - upload invalid file type
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "malicious data".getBytes());

        mockMvc.perform(multipart("/api/posts/{id}/content", postId)
                .file(invalidFile)
                .param("description", "Invalid file")
                .param("contentType", ContentType.DOCUMENT.name())
                .param("storeInDb", "true")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        // Test permission - reader shouldn't be able to upload content
        mockMvc.perform(multipart("/api/posts/{id}/content", postId)
                .file(imageFile)
                .param("description", "Test reader upload")
                .param("contentType", ContentType.IMAGE.name())
                .param("storeInDb", "true")
                .header("Authorization", "Bearer " + readerToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());

        // Delete post content
        mockMvc.perform(delete("/api/posts/{postId}/content/{contentId}", postId, postContentId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // Verify post content is deleted
        mockMvc.perform(get("/api/posts/{id}/content", postId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // Delete comment content
        mockMvc.perform(delete("/api/comments/{commentId}/content/{contentId}", commentId, commentContentId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // Verify comment content is deleted
        mockMvc.perform(get("/api/comments/{id}/content", commentId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    /**
     * Test search functionality endpoints
     */
    @Test
    void testSearchEndpoints() throws Exception {
        // Create forums with searchable names
        CreateForumRequest techForumRequest = CreateForumRequest.builder()
                .name("Technology Forum API")
                .description("Forum about technology")
                .build();

        CreateForumRequest scienceForumRequest = CreateForumRequest.builder()
                .name("Science Forum API")
                .description("Forum about science")
                .build();
        Long techForumId = extractLongId(mockMvc.perform(post("/api/forums")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(techForumRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        Long scienceForumId = extractLongId(mockMvc.perform(post("/api/forums")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scienceForumRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        // Grant user access to both forums
        ForumAccessRequest accessRequest = ForumAccessRequest.builder()
                .userId(extractLongId(mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(), "$.id"))
                .accessLevel(AccessLevel.WRITE)
                .build();
        mockMvc.perform(post("/api/forums/{id}/access", techForumId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accessRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/forums/{id}/access", scienceForumId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accessRequest)))
                .andExpect(status().isOk());

        // Create posts with searchable content
        CreatePostRequest techPostRequest = CreatePostRequest.builder()
                .title("Artificial Intelligence")
                .content("AI is transforming technology")
                .forumId(techForumId)
                .build();

        CreatePostRequest sciencePostRequest = CreatePostRequest.builder()
                .title("Quantum Computing")
                .content("Quantum mechanics and computing")
                .forumId(scienceForumId)
                .build();

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(techPostRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sciencePostRequest)))
                .andExpect(status().isCreated());
        // Create comments with searchable content
        // Get post IDs for comments
        MvcResult techPostsResult = mockMvc.perform(get("/api/posts/by-forum/{forumId}", techForumId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult sciencePostsResult = mockMvc.perform(get("/api/posts/by-forum/{forumId}", scienceForumId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn();
        Long techPostId = extractLongId(techPostsResult.getResponse().getContentAsString(), "$[0].id");
        Long sciencePostId = extractLongId(sciencePostsResult.getResponse().getContentAsString(), "$[0].id");

        // Create comments
        Map<String, Object> aiCommentRequest = Map.of(
                "postId", techPostId,
                "content", "Neural networks are revolutionizing AI."
        );

        Map<String, Object> quantumCommentRequest = Map.of(
                "postId", sciencePostId,
                "content", "Quantum computing will break current encryption algorithms."
        );

        mockMvc.perform(post("/api/comments")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aiCommentRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/comments")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(quantumCommentRequest)))
                .andExpect(status().isCreated());

        // 1. Test forum search
        mockMvc.perform(get("/api/forums/search")
                .param("query", "technology")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Technology Forum API")))
                .andExpect(jsonPath("$", hasSize(1)));

        // Search with no results
        mockMvc.perform(get("/api/forums/search")
                .param("query", "nosuchforum")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 2. Test global post search
        mockMvc.perform(get("/api/posts/search")
                .param("query", "quantum")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Quantum Computing")))
                .andExpect(jsonPath("$", hasSize(1)));

        // Search for AI in posts
        mockMvc.perform(get("/api/posts/search")
                .param("query", "artificial intelligence")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Artificial Intelligence")))
                .andExpect(jsonPath("$", hasSize(1)));

        // 3. Test forum-specific post search
        mockMvc.perform(get("/api/posts/search")
                .param("query", "technology")
                .param("forumId", String.valueOf(techForumId))
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content", containsString("technology")))
                .andExpect(jsonPath("$", hasSize(1)));

        // 4. Test comment search
        mockMvc.perform(get("/api/comments/search")
                .param("query", "neural")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content", containsString("Neural networks")))
                .andExpect(jsonPath("$", hasSize(1)));

        // Search for encryption in comments
        mockMvc.perform(get("/api/comments/search")
                .param("query", "encryption")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content", containsString("encryption")))
                .andExpect(jsonPath("$", hasSize(1)));

        // 5. Test search with permissions
        // Create a forum with no access for regular user
        CreateForumRequest privateForumRequest = CreateForumRequest.builder()
                .name("Private Forum")
                .description("Forum with restricted access")
                .build();
        Long privateForumId = extractLongId(mockMvc
                .perform(post("/api/forums")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(privateForumRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        CreatePostRequest privatePostRequest = CreatePostRequest.builder()
                .title("Secret Information")
                .content("This is confidential information")
                .forumId(privateForumId)
                .build();

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(privatePostRequest)))
                .andExpect(status().isCreated());

        // Search for "confidential" - regular user shouldn't see private forum content
        mockMvc.perform(get("/api/posts/search")
                .param("query", "confidential")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Search as admin - should see private forum content
        mockMvc.perform(get("/api/posts/search")
                .param("query", "confidential")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content", containsString("confidential")))
                .andExpect(jsonPath("$", hasSize(1)));

        // Search with invalid parameters
        mockMvc.perform(get("/api/posts/search")
                .param("query", "")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());

        // Search without authentication
        mockMvc.perform(get("/api/posts/search")
                .param("query", "technology"))
                .andExpect(status().isUnauthorized());
    }
}
