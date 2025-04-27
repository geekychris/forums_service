package com.example.forum.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration class for file storage.
 */
@Configuration
public class StorageConfig implements WebMvcConfigurer {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>();
    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = new HashSet<>();
    private static final Set<String> ALLOWED_AUDIO_EXTENSIONS = new HashSet<>();
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = new HashSet<>();

    static {
        // Initialize allowed file extensions
        ALLOWED_IMAGE_EXTENSIONS.add(".jpg");
        ALLOWED_IMAGE_EXTENSIONS.add(".jpeg");
        ALLOWED_IMAGE_EXTENSIONS.add(".png");
        ALLOWED_IMAGE_EXTENSIONS.add(".gif");
        ALLOWED_IMAGE_EXTENSIONS.add(".webp");

        ALLOWED_DOCUMENT_EXTENSIONS.add(".pdf");
        ALLOWED_DOCUMENT_EXTENSIONS.add(".doc");
        ALLOWED_DOCUMENT_EXTENSIONS.add(".docx");
        ALLOWED_DOCUMENT_EXTENSIONS.add(".txt");
        ALLOWED_DOCUMENT_EXTENSIONS.add(".rtf");

        ALLOWED_AUDIO_EXTENSIONS.add(".mp3");
        ALLOWED_AUDIO_EXTENSIONS.add(".wav");
        ALLOWED_AUDIO_EXTENSIONS.add(".ogg");
        ALLOWED_AUDIO_EXTENSIONS.add(".m4a");

        ALLOWED_VIDEO_EXTENSIONS.add(".mp4");
        ALLOWED_VIDEO_EXTENSIONS.add(".webm");
        ALLOWED_VIDEO_EXTENSIONS.add(".avi");
        ALLOWED_VIDEO_EXTENSIONS.add(".mov");
    }

    @Value("${app.content.storage.path:./content-storage}")
    private String storagePath;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size:15MB}")
    private String maxRequestSize;

    /**
     * Initialize storage directory.
     */
    @PostConstruct
    public void init() {
        try {
            Path storageDirectory = Paths.get(storagePath);
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    /**
     * Configure resource handling for serving files.
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/content/**")
                .addResourceLocations("file:" + storagePath + "/");
    }

    /**
     * Multipart resolver bean.
     *
     * @return the multipart resolver
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * Load a file as a resource.
     *
     * @param filename the filename
     * @return the resource
     */
    public Resource loadAsResource(String filename) {
        try {
            Path file = Paths.get(storagePath).resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    /**
     * Check if file extension is allowed for images.
     *
     * @param filename the filename
     * @return true if allowed, false otherwise
     */
    public boolean isAllowedImage(String filename) {
        return hasAllowedExtension(filename, ALLOWED_IMAGE_EXTENSIONS);
    }

    /**
     * Check if file extension is allowed for documents.
     *
     * @param filename the filename
     * @return true if allowed, false otherwise
     */
    public boolean isAllowedDocument(String filename) {
        return hasAllowedExtension(filename, ALLOWED_DOCUMENT_EXTENSIONS);
    }

    /**
     * Check if file extension is allowed for audio.
     *
     * @param filename the filename
     * @return true if allowed, false otherwise
     */
    public boolean isAllowedAudio(String filename) {
        return hasAllowedExtension(filename, ALLOWED_AUDIO_EXTENSIONS);
    }

    /**
     * Check if file extension is allowed for video.
     *
     * @param filename the filename
     * @return true if allowed, false otherwise
     */
    public boolean isAllowedVideo(String filename) {
        return hasAllowedExtension(filename, ALLOWED_VIDEO_EXTENSIONS);
    }

    /**
     * Check if file extension is in the allowed set.
     *
     * @param filename  the filename
     * @param allowedExtensions the set of allowed extensions
     * @return true if allowed, false otherwise
     */
    private boolean hasAllowedExtension(String filename, Set<String> allowedExtensions) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        String filenameLower = filename.toLowerCase();
        for (String ext : allowedExtensions) {
            if (filenameLower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the storage path.
     *
     * @return the storage path
     */
    public String getStoragePath() {
        return storagePath;
    }

    /**
     * Get the maximum file size.
     *
     * @return the maximum file size
     */
    public String getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Get the maximum request size.
     *
     * @return the maximum request size
     */
    public String getMaxRequestSize() {
        return maxRequestSize;
    }
}

