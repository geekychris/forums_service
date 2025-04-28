package com.example.forum.cli.services;

import com.example.forum.cli.model.comment.CommentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final WebClient webClient;
    
    /**
     * Create a new comment on a post
     */
    public Object createComment(Long postId, String content) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("postId", postId);
        requestBody.put("content", content);

        return webClient.post()
                .uri("/api/comments")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(CommentResponse.class)
                .onErrorResume(e -> {
                    log.error("Error creating comment: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to create comment: " + e.getMessage()));
                })
                .block();
    }

    /**
     * Create a reply to an existing comment
     */
    public Object createReply(Long parentCommentId, String content) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("parentCommentId", parentCommentId);
        requestBody.put("content", content);

        return webClient.post()
                .uri("/api/comments")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(CommentResponse.class)
                .onErrorResume(e -> {
                    log.error("Error creating reply: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to create reply: " + e.getMessage()));
                })
                .block();
    }

    /**
     * Get a specific comment by ID
     */
    public Object getCommentById(Long id) {
        return webClient.get()
                .uri("/api/comments/{id}", id)
                .retrieve()
                .bodyToMono(CommentResponse.class)
                .onErrorResume(e -> {
                    log.error("Error getting comment: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get comment: " + e.getMessage()));
                })
                .block();
    }

    /**
     * Update an existing comment
     */
    public Object updateComment(Long id, String content) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", content);

        return webClient.put()
                .uri("/api/comments/{id}", id)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(CommentResponse.class)
                .onErrorResume(e -> {
                    log.error("Error updating comment: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to update comment: " + e.getMessage()));
                })
                .block();
    }

    /**
     * Delete a comment
     */
    public Object deleteComment(Long id) {
        return webClient.delete()
                .uri("/api/comments/{id}", id)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Error deleting comment: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to delete comment: " + e.getMessage()));
                })
                .block();
    }

    /**
     * Get comments for a specific post with pagination
     */
    public Object getCommentsByPost(Long postId, int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/comments/by-post/{postId}")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(postId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<CommentResponse>>() {})
                .onErrorResume(e -> {
                    log.error("Error getting comments for post: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get comments: " + e.getMessage()));
                })
                .block();
    }

    /**
     * Get replies to a specific comment with pagination
     */
    public Object getRepliesByComment(Long commentId, int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/comments/{commentId}/replies")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(commentId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<CommentResponse>>() {})
                .onErrorResume(e -> {
                    log.error("Error getting replies for comment: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get replies: " + e.getMessage()));
                })
                .block();
    }

    /**
     * Upvote a comment
     */
    public Object upvoteComment(Long id) {
        return webClient.post()
                .uri("/api/comments/{id}/upvote", id)
                .retrieve()
                .bodyToMono(CommentResponse.class)
                .onErrorResume(e -> {
                    log.error("Error upvoting comment: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to upvote comment: " + e.getMessage()));
                })
                .block();
    }

    /**
     * Downvote a comment
     */
    public Object downvoteComment(Long id) {
        return webClient.post()
                .uri("/api/comments/{id}/downvote", id)
                .retrieve()
                .bodyToMono(CommentResponse.class)
                .onErrorResume(e -> {
                    log.error("Error downvoting comment: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to downvote comment: " + e.getMessage()));
                })
                .block();
    }
}
