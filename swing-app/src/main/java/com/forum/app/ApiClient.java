package com.forum.app;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.swing.SwingWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Client for interacting with the Forum API.
 * This class handles all HTTP communication with the backend services.
 */
public class ApiClient {
    
    private static final String BASE_URL = "http://localhost:9090";
    private static final String API_PATH = "/api";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    // API Endpoints
    private static final String AUTH_LOGIN = API_PATH + "/auth/login";
    private static final String AUTH_REGISTER = API_PATH + "/auth/register";
    private static final String AUTH_ME = API_PATH + "/auth/me";
    
    private static final String FORUMS_BASE = API_PATH + "/forums";
    private static final String FORUMS_SEARCH = FORUMS_BASE + "/search";
    private static final String FORUMS_ACCESSIBLE = FORUMS_BASE + "/accessible";
    
    private static final String POSTS_BASE = API_PATH + "/posts";
    private static final String POSTS_BY_FORUM = POSTS_BASE + "/by-forum";
    private static final String POSTS_SEARCH = POSTS_BASE + "/search";
    
    private static final String COMMENTS_BASE = API_PATH + "/comments";
    private static final String COMMENTS_BY_POST = COMMENTS_BASE + "/by-post";
    
    /**
     * Private constructor to prevent instantiation.
     * All methods are static.
     */
    private ApiClient() {
        // Utility class, no instances
    }
    
    /**
     * Authenticate a user and retrieve a JWT token.
     * 
     * @param username Username
     * @param password Password
     * @param callback Callback to handle the authentication result
     */
    public static void login(String username, String password, 
            Function<ApiResponse<JSONObject>, Void> callback) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", username);
        requestBody.put("password", password);
        
        new SwingWorker<ApiResponse<JSONObject>, Void>() {
            @Override
            protected ApiResponse<JSONObject> doInBackground() throws Exception {
                return postJson(AUTH_LOGIN, requestBody, false);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<JSONObject> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Login failed: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Register a new user.
     * 
     * @param username Username
     * @param password Password
     * @param email Email
     * @param displayName Display name
     * @param callback Callback to handle the registration result
     */
    public static void register(String username, String password, String email, String displayName,
            Function<ApiResponse<JSONObject>, Void> callback) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", username);
        requestBody.put("password", password);
        requestBody.put("email", email);
        requestBody.put("displayName", displayName);
        
        new SwingWorker<ApiResponse<JSONObject>, Void>() {
            @Override
            protected ApiResponse<JSONObject> doInBackground() throws Exception {
                return postJson(AUTH_REGISTER, requestBody, false);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<JSONObject> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Registration failed: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Get information about the current authenticated user.
     * 
     * @param callback Callback to handle the result
     */
    public static void getCurrentUser(Function<ApiResponse<JSONObject>, Void> callback) {
        new SwingWorker<ApiResponse<JSONObject>, Void>() {
            @Override
            protected ApiResponse<JSONObject> doInBackground() throws Exception {
                return getJson(AUTH_ME, true);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<JSONObject> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to get user info: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Get all root-level forums.
     * 
     * @param callback Callback to handle the result
     */
    public static void getRootForums(Function<ApiResponse<List<ForumDto>>, Void> callback) {
        new SwingWorker<ApiResponse<List<ForumDto>>, Void>() {
            @Override
            protected ApiResponse<List<ForumDto>> doInBackground() throws Exception {
                ApiResponse<JSONArray> response = getJsonArray(FORUMS_BASE, false);
                
                if (!response.isSuccess()) {
                    return ApiResponse.error(response.getErrorMessage());
                }
                
                List<ForumDto> forums = new ArrayList<>();
                JSONArray jsonArray = response.getData();
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonForum = jsonArray.getJSONObject(i);
                    forums.add(jsonToForumDto(jsonForum));
                }
                
                return ApiResponse.success(forums);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<List<ForumDto>> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to get forums: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Get subforums for a specified parent forum.
     * 
     * @param parentId Parent forum ID
     * @param callback Callback to handle the result
     */
    public static void getSubforums(long parentId, Function<ApiResponse<List<ForumDto>>, Void> callback) {
        new SwingWorker<ApiResponse<List<ForumDto>>, Void>() {
            @Override
            protected ApiResponse<List<ForumDto>> doInBackground() throws Exception {
                ApiResponse<JSONArray> response = getJsonArray(FORUMS_BASE + "/" + parentId + "/subforums", false);
                
                if (!response.isSuccess()) {
                    return ApiResponse.error(response.getErrorMessage());
                }
                
                List<ForumDto> forums = new ArrayList<>();
                JSONArray jsonArray = response.getData();
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonForum = jsonArray.getJSONObject(i);
                    forums.add(jsonToForumDto(jsonForum));
                }
                
                return ApiResponse.success(forums);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<List<ForumDto>> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to get subforums: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Create a new forum.
     * 
     * @param name Forum name
     * @param description Forum description
     * @param parentId Parent forum ID (null for root-level forums)
     * @param callback Callback to handle the result
     */
    public static void createForum(String name, String description, Long parentId,
            Function<ApiResponse<ForumDto>, Void> callback) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", name);
        requestBody.put("description", description);
        
        String endpoint = parentId == null 
                ? FORUMS_BASE 
                : FORUMS_BASE + "/" + parentId + "/subforums";
        
        new SwingWorker<ApiResponse<ForumDto>, Void>() {
            @Override
            protected ApiResponse<ForumDto> doInBackground() throws Exception {
                ApiResponse<JSONObject> response = postJson(endpoint, requestBody, true);
                
                if (!response.isSuccess()) {
                    return ApiResponse.error(response.getErrorMessage());
                }
                
                ForumDto forum = jsonToForumDto(response.getData());
                return ApiResponse.success(forum);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<ForumDto> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to create forum: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Update a forum.
     * 
     * @param forumId Forum ID
     * @param name New forum name
     * @param description New forum description
     * @param callback Callback to handle the result
     */
    public static void updateForum(long forumId, String name, String description,
            Function<ApiResponse<ForumDto>, Void> callback) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", name);
        requestBody.put("description", description);
        
        new SwingWorker<ApiResponse<ForumDto>, Void>() {
            @Override
            protected ApiResponse<ForumDto> doInBackground() throws Exception {
                ApiResponse<JSONObject> response = putJson(FORUMS_BASE + "/" + forumId, requestBody, true);
                
                if (!response.isSuccess()) {
                    return ApiResponse.error(response.getErrorMessage());
                }
                
                ForumDto forum = jsonToForumDto(response.getData());
                return ApiResponse.success(forum);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<ForumDto> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to update forum: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Delete a forum.
     * 
     * @param forumId Forum ID
     * @param callback Callback to handle the result
     */
    public static void deleteForum(long forumId, Function<ApiResponse<String>, Void> callback) {
        new SwingWorker<ApiResponse<String>, Void>() {
            @Override
            protected ApiResponse<String> doInBackground() throws Exception {
                return deleteRequest(FORUMS_BASE + "/" + forumId, true);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<String> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to delete forum: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Get posts for a specified forum.
     * 
     * @param forumId Forum ID
     * @param page Page number (0-based)
     * @param size Page size
     * @param callback Callback to handle the result
     */
    public static void getPostsByForum(long forumId, int page, int size,
            Function<ApiResponse<List<PostDto>>, Void> callback) {
        new SwingWorker<ApiResponse<List<PostDto>>, Void>() {
            @Override
            protected ApiResponse<List<PostDto>> doInBackground() throws Exception {
                String url = POSTS_BY_FORUM + "/" + forumId + "?page=" + page + "&size=" + size;
                ApiResponse<JSONArray> response = getJsonArray(url, true);
                
                if (!response.isSuccess()) {
                    return ApiResponse.error(response.getErrorMessage());
                }
                
                List<PostDto> posts = new ArrayList<>();
                JSONArray jsonArray = response.getData();
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonPost = jsonArray.getJSONObject(i);
                    posts.add(jsonToPostDto(jsonPost));
                }
                
                return ApiResponse.success(posts);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<List<PostDto>> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to get posts: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Create a new post in a forum.
     * 
     * @param title Post title
     * @param content Post content
     * @param forumId Forum ID
     * @param callback Callback to handle the result
     */
    public static void createPost(String title, String content, long forumId,
            Function<ApiResponse<PostDto>, Void> callback) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("title", title);
        requestBody.put("content", content);
        requestBody.put("forumId", forumId);
        
        new SwingWorker<ApiResponse<PostDto>, Void>() {
            @Override
            protected ApiResponse<PostDto> doInBackground() throws Exception {
                ApiResponse<JSONObject> response = postJson(POSTS_BASE, requestBody, true);
                
                if (!response.isSuccess()) {
                    return ApiResponse.error(response.getErrorMessage());
                }
                
                PostDto post = jsonToPostDto(response.getData());
                return ApiResponse.success(post);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<PostDto> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to create post: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Update a post.
     * 
     * @param postId Post ID
     * @param title New post title
     * @param content New post content
     * @param callback Callback to handle the result
     */
    public static void updatePost(long postId, String title, String content,
            Function<ApiResponse<PostDto>, Void> callback) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("title", title);
        requestBody.put("content", content);
        
        new SwingWorker<ApiResponse<PostDto>, Void>() {
            @Override
            protected ApiResponse<PostDto> doInBackground() throws Exception {
                ApiResponse<JSONObject> response = putJson(POSTS_BASE + "/" + postId, requestBody, true);
                
                if (!response.isSuccess()) {
                    return ApiResponse.error(response.getErrorMessage());
                }
                
                PostDto post = jsonToPostDto(response.getData());
                return ApiResponse.success(post);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<PostDto> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to update post: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Delete a post.
     * 
     * @param postId Post ID
     * @param callback Callback to handle the result
     */
    public static void deletePost(long postId, Function<ApiResponse<String>, Void> callback) {
        new SwingWorker<ApiResponse<String>, Void>() {
            @Override
            protected ApiResponse<String> doInBackground() throws Exception {
                return deleteRequest(POSTS_BASE + "/" + postId, true);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<String> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to delete post: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Get comments for a post.
     * 
     * @param postId Post ID
     * @param page Page number (0-based)
     * @param size Page size
     * @param callback Callback to handle the result
     */
    public static void getCommentsByPost(long postId, int page, int size,
            Function<ApiResponse<List<CommentDto>>, Void> callback) {
        new SwingWorker<ApiResponse<List<CommentDto>>, Void>() {
            @Override
            protected ApiResponse<List<CommentDto>> doInBackground() throws Exception {
                String url = COMMENTS_BY_POST + "/" + postId + "?page=" + page + "&size=" + size;
                ApiResponse<JSONArray> response = getJsonArray(url, true);
                
                if (!response.isSuccess()) {
                    return ApiResponse.error(response.getErrorMessage());
                }
                
                List<CommentDto> comments = new ArrayList<>();
                JSONArray jsonArray = response.getData();
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonComment = jsonArray.getJSONObject(i);
                    comments.add(jsonToCommentDto(jsonComment));
                }
                
                return ApiResponse.success(comments);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<List<CommentDto>> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to get comments: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Create a new comment on a post.
     * 
     * @param content Comment content
     * @param postId Post ID
     * @param parentCommentId Parent comment ID (for replies, can be null)
     * @param callback Callback to handle the result
     */
    public static void createComment(String content, long postId, Long parentCommentId,
            Function<ApiResponse<CommentDto>, Void> callback) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("content", content);
        requestBody.put("postId", postId);
        if (parentCommentId != null) {
            requestBody.put("parentCommentId", parentCommentId);
        }
        
        new SwingWorker<ApiResponse<CommentDto>, Void>() {
            @Override
            protected ApiResponse<CommentDto> doInBackground() throws Exception {
                ApiResponse<JSONObject> response = postJson(COMMENTS_BASE, requestBody, true);
                
                if (!response.isSuccess()) {
                    return ApiResponse.error(response.getErrorMessage());
                }
                
                CommentDto comment = jsonToCommentDto(response.getData());
                return ApiResponse.success(comment);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<CommentDto> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to create comment: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    //
    // HTTP Utility Methods
    //
    
    /**
     * Perform a GET request and return a JSON object response.
     * 
     * @param endpoint API endpoint
     * @param authenticated Whether the request requires authentication
     * @return Response containing a JSON object
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws JSONException If JSON parsing fails
     */
    private static ApiResponse<JSONObject> getJson(String endpoint, boolean authenticated) 
            throws IOException, InterruptedException, JSONException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .header("Accept", "application/json");
        
        if (authenticated) {
            String token = LoginPanel.getJwtToken();
            if (token == null) {
                return ApiResponse.error("Not authenticated");
            }
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        
        HttpRequest request = requestBuilder.build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JSONObject jsonResponse = new JSONObject(response.body());
            return ApiResponse.success(jsonResponse);
        } else {
            return ApiResponse.error("HTTP error: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Perform a GET request and return a JSON array response.
     * 
     * @param endpoint API endpoint
     * @param authenticated Whether the request requires authentication
     * @return Response containing a JSON array
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws JSONException If JSON parsing fails
     */
    private static ApiResponse<JSONArray> getJsonArray(String endpoint, boolean authenticated) 
            throws IOException, InterruptedException, JSONException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .header("Accept", "application/json");
        
        if (authenticated) {
            String token = LoginPanel.getJwtToken();
            if (token == null) {
                return ApiResponse.error("Not authenticated");
            }
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        
        HttpRequest request = requestBuilder.build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JSONArray jsonResponse = new JSONArray(response.body());
            return ApiResponse.success(jsonResponse);
        } else {
            return ApiResponse.error("HTTP error: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Perform a POST request with a JSON body.
     * 
     * @param endpoint API endpoint
     * @param jsonBody JSON request body
     * @param authenticated Whether the request requires authentication
     * @return Response containing a JSON object
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws JSONException If JSON parsing fails
     */
    private static ApiResponse<JSONObject> postJson(String endpoint, JSONObject jsonBody, boolean authenticated) 
            throws IOException, InterruptedException, JSONException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()));
        
        if (authenticated) {
            String token = LoginPanel.getJwtToken();
            if (token == null) {
                return ApiResponse.error("Not authenticated");
            }
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        
        HttpRequest request = requestBuilder.build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JSONObject jsonResponse = new JSONObject(response.body());
            return ApiResponse.success(jsonResponse);
        } else {
            return ApiResponse.error("HTTP error: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Perform a PUT request with a JSON body.
     * 
     * @param endpoint API endpoint
     * @param jsonBody JSON request body
     * @param authenticated Whether the request requires authentication
     * @return Response containing a JSON object
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws JSONException If JSON parsing fails
     */
    private static ApiResponse<JSONObject> putJson(String endpoint, JSONObject jsonBody, boolean authenticated) 
            throws IOException, InterruptedException, JSONException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody.toString()));
        
        if (authenticated) {
            String token = LoginPanel.getJwtToken();
            if (token == null) {
                return ApiResponse.error("Not authenticated");
            }
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        
        HttpRequest request = requestBuilder.build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JSONObject jsonResponse = new JSONObject(response.body());
            return ApiResponse.success(jsonResponse);
        } else {
            return ApiResponse.error("HTTP error: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Perform a DELETE request.
     * 
     * @param endpoint API endpoint
     * @param authenticated Whether the request requires authentication
     * @return Response containing a success message
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     */
    private static ApiResponse<String> deleteRequest(String endpoint, boolean authenticated) 
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .DELETE();
        
        if (authenticated) {
            String token = LoginPanel.getJwtToken();
            if (token == null) {
                return ApiResponse.error("Not authenticated");
            }
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        
        HttpRequest request = requestBuilder.build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return ApiResponse.success("Operation completed successfully");
        } else {
            return ApiResponse.error("HTTP error: " + response.statusCode() + " - " + response.body());
        }
    }
    //
    // DTO Conversion Methods
    //
    
    /**
     * Convert a JSON object to a ForumDto.
     * 
     * @param json JSON object containing forum data
     * @return ForumDto object
     */
    private static ForumDto jsonToForumDto(JSONObject json) {
        ForumDto forum = new ForumDto();
        
        forum.setId(json.getLong("id"));
        forum.setName(json.getString("name"));
        forum.setDescription(json.optString("description", ""));
        
        if (!json.isNull("parentForumId")) {
            forum.setParentForumId(json.getLong("parentForumId"));
        }
        
        forum.setParentForumName(json.optString("parentForumName", null));
        
        // Parse dates
        if (json.has("createdAt") && !json.isNull("createdAt")) {
            forum.setCreatedAt(LocalDateTime.parse(json.getString("createdAt")));
        }
        
        if (json.has("updatedAt") && !json.isNull("updatedAt")) {
            forum.setUpdatedAt(LocalDateTime.parse(json.getString("updatedAt")));
        }
        
        forum.setPostCount(json.optInt("postCount", 0));
        
        // Parse permissions
        forum.setCanRead(json.optBoolean("canRead", true));
        forum.setCanWrite(json.optBoolean("canWrite", false));
        forum.setCanAdmin(json.optBoolean("canAdmin", false));
        
        // Parse subforums if present
        if (json.has("subForums") && !json.isNull("subForums")) {
            JSONArray subForumsJson = json.getJSONArray("subForums");
            for (int i = 0; i < subForumsJson.length(); i++) {
                JSONObject subForumJson = subForumsJson.getJSONObject(i);
                forum.addSubForum(jsonToForumDto(subForumJson));
            }
        }
        
        return forum;
    }
    
    /**
     * Convert a JSON object to a PostDto.
     * 
     * @param json JSON object containing post data
     * @return PostDto object
     */
    private static PostDto jsonToPostDto(JSONObject json) {
        PostDto post = new PostDto();
        
        post.setId(json.getLong("id"));
        post.setTitle(json.getString("title"));
        post.setContent(json.getString("content"));
        
        // Parse dates
        if (json.has("createdAt") && !json.isNull("createdAt")) {
            post.setCreatedAt(LocalDateTime.parse(json.getString("createdAt")));
        }
        
        if (json.has("updatedAt") && !json.isNull("updatedAt")) {
            post.setUpdatedAt(LocalDateTime.parse(json.getString("updatedAt")));
        }
        
        // Parse author
        if (json.has("author") && !json.isNull("author")) {
            post.setAuthor(jsonToUserDto(json.getJSONObject("author")));
        }
        
        post.setForumId(json.getLong("forumId"));
        post.setForumName(json.optString("forumName", ""));
        post.setCommentCount(json.optInt("commentCount", 0));
        
        // Parse permissions
        post.setCanEdit(json.optBoolean("canEdit", false));
        post.setCanDelete(json.optBoolean("canDelete", false));
        
        // Parse contents if present
        if (json.has("contents") && !json.isNull("contents")) {
            JSONArray contentsJson = json.getJSONArray("contents");
            for (int i = 0; i < contentsJson.length(); i++) {
                JSONObject contentJson = contentsJson.getJSONObject(i);
                post.addContent(jsonToPostContentDto(contentJson));
            }
        }
        
        return post;
    }
    
    /**
     * Convert a JSON object to a CommentDto.
     * 
     * @param json JSON object containing comment data
     * @return CommentDto object
     */
    private static CommentDto jsonToCommentDto(JSONObject json) {
        CommentDto comment = new CommentDto();
        
        comment.setId(json.getLong("id"));
        comment.setContent(json.getString("content"));
        
        // Parse dates
        if (json.has("createdAt") && !json.isNull("createdAt")) {
            comment.setCreatedAt(LocalDateTime.parse(json.getString("createdAt")));
        }
        
        if (json.has("updatedAt") && !json.isNull("updatedAt")) {
            comment.setUpdatedAt(LocalDateTime.parse(json.getString("updatedAt")));
        }
        
        // Parse author
        if (json.has("author") && !json.isNull("author")) {
            comment.setAuthor(jsonToUserDto(json.getJSONObject("author")));
        }
        
        comment.setPostId(json.getLong("postId"));
        
        if (!json.isNull("parentCommentId")) {
            comment.setParentCommentId(json.getLong("parentCommentId"));
        }
        
        comment.setReplyCount(json.optInt("replyCount", 0));
        
        // Parse permissions
        comment.setCanEdit(json.optBoolean("canEdit", false));
        comment.setCanDelete(json.optBoolean("canDelete", false));
        
        // Parse contents if present
        if (json.has("contents") && !json.isNull("contents")) {
            JSONArray contentsJson = json.getJSONArray("contents");
            for (int i = 0; i < contentsJson.length(); i++) {
                JSONObject contentJson = contentsJson.getJSONObject(i);
                comment.addContent(jsonToCommentContentDto(contentJson));
            }
        }
        
        return comment;
    }
    
    /**
     * Convert a JSON object to a UserDto.
     * 
     * @param json JSON object containing user data
     * @return UserDto object
     */
    private static UserDto jsonToUserDto(JSONObject json) {
        UserDto user = new UserDto();
        
        user.setId(json.getLong("id"));
        user.setUsername(json.getString("username"));
        user.setEmail(json.optString("email", ""));
        user.setDisplayName(json.optString("displayName", ""));
        user.setRole(json.optString("role", "USER"));
        user.setActive(json.optBoolean("active", true));
        
        if (json.has("createdAt") && !json.isNull("createdAt")) {
            user.setCreatedAt(LocalDateTime.parse(json.getString("createdAt")));
        }
        
        return user;
    }
    
    /**
     * Convert a JSON object to a PostContentDto.
     * 
     * @param json JSON object containing post content data
     * @return PostContentDto object
     */
    private static PostContentDto jsonToPostContentDto(JSONObject json) {
        PostContentDto content = new PostContentDto();
        
        content.setId(json.getLong("id"));
        content.setFilename(json.optString("filename", ""));
        content.setDescription(json.optString("description", ""));
        
        // Parse content type
        if (json.has("contentType") && !json.isNull("contentType")) {
            String typeStr = json.getString("contentType");
            try {
                content.setContentType(PostContentDto.ContentType.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                // Default to IMAGE if type is unknown
                content.setContentType(PostContentDto.ContentType.IMAGE);
            }
        }
        
        content.setContentUrl(json.optString("contentUrl", ""));
        
        // Parse date
        if (json.has("createdAt") && !json.isNull("createdAt")) {
            content.setCreatedAt(LocalDateTime.parse(json.getString("createdAt")));
        }
        
        return content;
    }
    
    /**
     * Convert a JSON object to a CommentContentDto.
     * 
     * @param json JSON object containing comment content data
     * @return CommentContentDto object
     */
    private static CommentContentDto jsonToCommentContentDto(JSONObject json) {
        CommentContentDto content = new CommentContentDto();
        
        content.setId(json.getLong("id"));
        content.setFilename(json.optString("filename", ""));
        content.setDescription(json.optString("description", ""));
        
        // Parse content type
        if (json.has("contentType") && !json.isNull("contentType")) {
            String typeStr = json.getString("contentType");
            try {
                content.setContentType(PostContentDto.ContentType.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                // Default to IMAGE if type is unknown
                content.setContentType(PostContentDto.ContentType.IMAGE);
            }
        }
        
        content.setContentUrl(json.optString("contentUrl", ""));
        
        // Parse date
        if (json.has("createdAt") && !json.isNull("createdAt")) {
            content.setCreatedAt(LocalDateTime.parse(json.getString("createdAt")));
        }
        
        return content;
    }
    
    /**
     * Update a comment.
     * 
     * @param commentId Comment ID
     * @param content New comment content
     * @param callback Callback to handle the result
     */
    public static void updateComment(long commentId, String content,
            Function<ApiResponse<CommentDto>, Void> callback) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("content", content);
        
        new SwingWorker<ApiResponse<CommentDto>, Void>() {
            @Override
            protected ApiResponse<CommentDto> doInBackground() throws Exception {
                ApiResponse<JSONObject> response = putJson(COMMENTS_BASE + "/" + commentId, requestBody, true);
                
                if (!response.isSuccess()) {
                    return ApiResponse.error(response.getErrorMessage());
                }
                
                CommentDto comment = jsonToCommentDto(response.getData());
                return ApiResponse.success(comment);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<CommentDto> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to update comment: " + e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Get a forum by ID.
     * 
     * @param forumId Forum ID
     * @param callback Callback to handle the result
     */
    public static void getForumById(long forumId, Function<ApiResponse<ForumDto>, Void> callback) {
        new SwingWorker<ApiResponse<ForumDto>, Void>() {
            @Override
            protected ApiResponse<ForumDto> doInBackground() throws Exception {
                ApiResponse<JSONObject> response = getJson(FORUMS_BASE + "/" + forumId, false);
                
                if (!response.isSuccess()) {
                    return ApiResponse.error(response.getErrorMessage());
                }
                
                ForumDto forum = jsonToForumDto(response.getData());
                return ApiResponse.success(forum);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse<ForumDto> response = get();
                    callback.apply(response);
                } catch (Exception e) {
                    callback.apply(ApiResponse.error("Failed to get forum: " + e.getMessage()));
                }
            }
        }.execute();
    }
}
