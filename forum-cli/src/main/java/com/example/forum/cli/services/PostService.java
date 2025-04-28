package com.example.forum.cli.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final HttpGraphQlClient graphQlClient;

    public Object createPost(String forumId, String title, String content) {
        String mutation = """
            mutation CreatePost($forumId: ID!, $title: String!, $content: String!) {
              createPost(forumId: $forumId, title: $title, content: $content) {
                id
                title
                content
                createdAt
                updatedAt
                forumId
                forumName
                author {
                  id
                  username
                  displayName
                }
                commentCount
                canEdit
                canDelete
              }
            }
        """;

        return graphQlClient.document(mutation)
                .variables(Map.of(
                        "forumId", forumId,
                        "title", title,
                        "content", content))
                .retrieve("createPost")
                .toEntity(Object.class)
                .onErrorResume(e -> {
                    log.error("Error creating post: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to create post: " + e.getMessage()));
                })
                .block();
    }

    public Object getPostsByForum(String forumId, int page, int size) {
        String query = """
            query GetPosts($forumId: ID!, $page: Int!, $size: Int!) {
              posts(forumId: $forumId, page: $page, size: $size) {
                content {
                  id
                  title
                  content
                  createdAt
                  updatedAt
                  forumId
                  forumName
                  commentCount
                  author {
                    id
                    username
                    displayName
                  }
                  contents {
                    id
                    filename
                    contentUrl
                  }
                  canEdit
                  canDelete
                }
                totalElements
                totalPages
                size
                number
                hasNext
                hasPrevious
              }
            }
        """;

        return graphQlClient.document(query)
                .variables(Map.of(
                        "forumId", forumId,
                        "page", page,
                        "size", size))
                .retrieve("posts")
                .toEntity(Object.class)
                .onErrorResume(e -> {
                    log.error("Error getting posts: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get posts: " + e.getMessage()));
                })
                .block();
    }

    public Object getPostById(String id) {
        String query = """
            query GetPost($id: ID!) {
              post(id: $id) {
                id
                title
                content
                createdAt
                updatedAt
                forumId
                forumName
                commentCount
                author {
                  id
                  username
                  displayName
                }
                contents {
                  id
                  filename
                  contentUrl
                }
                canEdit
                canDelete
              }
            }
        """;

        return graphQlClient.document(query)
                .variables(Map.of("id", id))
                .retrieve("post")
                .toEntity(Object.class)
                .onErrorResume(e -> {
                    log.error("Error getting post: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get post: " + e.getMessage()));
                })
                .block();
    }

    public Object updatePost(String id, String title, String content) {
        String mutation = """
            mutation UpdatePost($id: ID!, $title: String, $content: String) {
              updatePost(id: $id, title: $title, content: $content) {
                id
                title
                content
                updatedAt
                createdAt
                forumId
                forumName
                commentCount
                author {
                  id
                  username
                  displayName
                }
                canEdit
                canDelete
              }
            }
        """;

        Map<String, Object> variables = Map.of(
                "id", id,
                "title", title != null ? title : "",
                "content", content != null ? content : "");

        return graphQlClient.document(mutation)
                .variables(variables)
                .retrieve("updatePost")
                .toEntity(Object.class)
                .onErrorResume(e -> {
                    log.error("Error updating post: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to update post: " + e.getMessage()));
                })
                .block();
    }

    public Object deletePost(String id) {
        String mutation = """
            mutation DeletePost($id: ID!) {
              deletePost(id: $id)
            }
        """;

        return graphQlClient.document(mutation)
                .variables(Map.of("id", id))
                .retrieve("deletePost")
                .toEntity(Boolean.class)
                .onErrorResume(e -> {
                    log.error("Error deleting post: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to delete post: " + e.getMessage()));
                })
                .block();
    }

    public Object getPostsByUser(String userId, int page, int size) {
        String query = """
            query GetUserPosts($userId: ID!, $page: Int!, $size: Int!) {
              userPosts(authorId: $userId, page: $page, size: $size) {
                content {
                  id
                  title
                  content
                  createdAt
                  updatedAt
                  forumId
                  forumName
                  commentCount
                  author {
                    id
                    username
                    displayName
                  }
                  contents {
                    id
                    filename
                    contentUrl
                  }
                  canEdit
                  canDelete
                }
                totalElements
                totalPages
                size
                number
              }
            }
        """;

        return graphQlClient.document(query)
                .variables(Map.of(
                        "userId", userId,
                        "page", page,
                        "size", size))
                .retrieve("userPosts")
                .toEntity(Object.class)
                .onErrorResume(e -> {
                    log.error("Error getting user posts: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get user posts: " + e.getMessage()));
                })
                .block();
    }
}
