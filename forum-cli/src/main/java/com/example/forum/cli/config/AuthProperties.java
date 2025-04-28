package com.example.forum.cli.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for authentication.
 */
@Component
@ConfigurationProperties(prefix = "forum.auth")
@Data
public class AuthProperties {
    
    /**
     * Path to the file storing the authentication token
     */
    private String tokenFile;
}

