package com.example.forum.cli.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for API connections.
 */
@Component
@ConfigurationProperties(prefix = "forum.api")
@Data
public class ApiProperties {
    
    /**
     * Base URL for the Forum API
     */
    private String baseUrl = "http://localhost:8081/api";
    
    /**
     * GraphQL endpoint URL
     */
    private String graphqlEndpoint;
    
    /**
     * Connection timeout in milliseconds
     */
    private int restConnectTimeout = 5000;
    
    /**
     * Read timeout in milliseconds
     */
    private int restReadTimeout = 15000;
}

