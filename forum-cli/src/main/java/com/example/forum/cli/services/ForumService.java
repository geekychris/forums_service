package com.example.forum.cli.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumService {

    private final WebClient webClient;

    public Object createForum(String name, String description) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", name);
        requestBody.put("description", description);

        return webClient.post()
                .uri("/api/forums")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error creating forum: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to create forum: " + e.getMessage()));
                })
                .block();
    }

    public Object getRootForums() {
        return webClient.get()
                .uri("/api/forums")
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error getting root forums: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get forums: " + e.getMessage()));
                })
                .block();
    }

    public Object getForumById(Long id) {
        return webClient.get()
                .uri("/api/forums/{id}", id)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error getting forum: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get forum: " + e.getMessage()));
                })
                .block();
    }

    public Object updateForum(Long id, String name, String description) {
        Map<String, Object> requestBody = new HashMap<>();
        if (name != null && !name.isEmpty()) {
            requestBody.put("name", name);
        }
        if (description != null && !description.isEmpty()) {
            requestBody.put("description", description);
        }

        return webClient.put()
                .uri("/api/forums/{id}", id)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error updating forum: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to update forum: " + e.getMessage()));
                })
                .block();
    }

    public Object deleteForum(Long id) {
        return webClient.delete()
                .uri("/api/forums/{id}", id)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Error deleting forum: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to delete forum: " + e.getMessage()));
                })
                .block();
    }

    public Object createSubforum(Long parentId, String name, String description) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", name);
        requestBody.put("description", description);

        return webClient.post()
                .uri("/api/forums/{parentId}/subforums", parentId)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error creating subforum: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to create subforum: " + e.getMessage()));
                })
                .block();
    }

    public Object getSubforums(Long parentId) {
        return webClient.get()
                .uri("/api/forums/{parentId}/subforums", parentId)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error getting subforums: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get subforums: " + e.getMessage()));
                })
                .block();
    }

    public Object searchForums(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/forums/search")
                        .queryParam("query", query)
                        .build())
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error searching forums: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to search forums: " + e.getMessage()));
                })
                .block();
    }

    public Object moveForum(Long id, Long parentId) {
        return webClient.put()
                .uri(uriBuilder -> {
                    if (parentId != null) {
                        return uriBuilder.path("/api/forums/{id}/move")
                                .queryParam("newParentId", parentId)
                                .build(id);
                    } else {
                        return uriBuilder.path("/api/forums/{id}/move")
                                .build(id);
                    }
                })
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error moving forum: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to move forum: " + e.getMessage()));
                })
                .block();
    }

    public Object grantForumAccess(Long forumId, Long userId, String accessLevel) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userId", userId);
        requestBody.put("accessLevel", accessLevel);

        return webClient.post()
                .uri("/api/forums/{forumId}/access", forumId)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Error granting forum access: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to grant access: " + e.getMessage()));
                })
                .block();
    }

    public Object revokeForumAccess(Long forumId, Long userId) {
        return webClient.delete()
                .uri("/api/forums/{forumId}/access/{userId}", forumId, userId)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Error revoking forum access: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to revoke access: " + e.getMessage()));
                })
                .block();
    }
}

