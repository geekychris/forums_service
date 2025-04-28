package com.example.forum.cli.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    private final WebClient webClient;

    public Object uploadPostContent(Long postId, File file, String contentType, String description, boolean storeInDb) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new FileSystemResource(file));
        bodyBuilder.part("type", contentType);
        
        if (description != null && !description.isEmpty()) {
            bodyBuilder.part("description", description);
        }
        
        bodyBuilder.part("storeInDb", String.valueOf(storeInDb));
        bodyBuilder.part("postId", postId.toString());

        return webClient.post()
                .uri("/api/content/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error uploading content to post: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to upload content: " + e.getMessage()));
                })
                .block();
    }

    public Object uploadCommentContent(Long commentId, File file, String contentType, String description, boolean storeInDb) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new FileSystemResource(file));
        bodyBuilder.part("contentType", contentType);
        
        if (description != null && !description.isEmpty()) {
            bodyBuilder.part("description", description);
        }
        
        bodyBuilder.part("storeInDb", String.valueOf(storeInDb));

        return webClient.post()
                .uri("/api/comments/{commentId}/content", commentId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error uploading content to comment: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to upload content: " + e.getMessage()));
                })
                .block();
    }

    public Object getPostContent(Long postId) {
        return webClient.get()
                .uri("/api/posts/{postId}/content", postId)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error getting post content: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get post content: " + e.getMessage()));
                })
                .block();
    }

    public Object getCommentContent(Long commentId) {
        return webClient.get()
                .uri("/api/comments/{commentId}/content", commentId)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    log.error("Error getting comment content: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get comment content: " + e.getMessage()));
                })
                .block();
    }

    public Object deleteContent(Long contentId) {
        return webClient.delete()
                .uri("/api/content/{contentId}", contentId)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Error deleting content: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to delete content: " + e.getMessage()));
                })
                .block();
    }
}
