        // Grant access to regular user
        ForumAccessRequest accessRequest = ForumAccessRequest.builder()
                .userId(JsonPath.read(mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(), "$.id"))
                .accessLevel(AccessLevel.WRITE)
                .build();
        // Create a post
        CreatePostRequest createPostRequest = CreatePostRequest.builder()
                .title("Test API Post")
                .content("This is a test post created through the API")
                .forumId(forumId)
                .build();
        // Grant access to regular user
        ForumAccessRequest accessRequest = ForumAccessRequest.builder()
                .userId(JsonPath.read(mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(), "$.id"))
                .accessLevel(AccessLevel.WRITE)
                .build();
        // Create a post
        CreatePostRequest createPostRequest = CreatePostRequest.builder()
                .title("Post for Comments")
                .content("This post will have comments")
                .forumId(forumId)
                .build();
        // Grant access to regular user
        ForumAccessRequest accessRequest = ForumAccessRequest.builder()
                .userId(JsonPath.read(mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(), "$.id"))
                .accessLevel(AccessLevel.WRITE)
                .build();
        // Create a post
        CreatePostRequest createPostRequest = CreatePostRequest.builder()
                .title("Post for Content")
                .content("This post will have content attachments")
                .forumId(forumId)
                .build();
        // Grant user access to both forums
        ForumAccessRequest accessRequest = ForumAccessRequest.builder()
                .userId(JsonPath.read(mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(), "$.id"))
                .accessLevel(AccessLevel.WRITE)
                .build();
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
                .param("query", "technology")
                .param("forumId", techForumId.toString())
        // Create a post in the private forum
        CreatePostRequest privatePostRequest = CreatePostRequest.builder()
                .title("Secret Information")
                .content("This is confidential information")
                .forumId(privateForumId)
                .build();
