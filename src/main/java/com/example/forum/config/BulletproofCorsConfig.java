package com.example.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Configuration for Cross-Origin Resource Sharing (CORS).
 * This class provides beans for both Spring Security CORS handling
 * and a direct CorsFilter for non-security CORS requirements.
 */
@Configuration
public class BulletproofCorsConfig {

    /**
     * Creates a CorsConfiguration with proper settings for the application.
     * 
     * @return CorsConfiguration with the appropriate CORS settings
     */
    private CorsConfiguration corsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Allowed origins
        config.addAllowedOrigin("http://localhost:3000");
        
        // Allow credentials
        config.setAllowCredentials(true);
        
        // Expose authentication headers to frontend
        config.addExposedHeader("Authorization");
        config.addExposedHeader("accessToken");
        
        // Allow common HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Allow common headers
        config.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "Accept", 
            "Origin", 
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"
        ));
        
        // Cache preflight response for 3600 seconds (1 hour)
        config.setMaxAge(3600L);
        
        return config;
    }
    
    /**
     * Creates a CorsConfigurationSource bean for use by Spring Security.
     * This is used by Spring Security's CORS handling through the .cors() configuration.
     * 
     * @return CorsConfigurationSource with CORS configuration for all endpoints
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Apply CORS configuration to all paths
        source.registerCorsConfiguration("/**", corsConfiguration());
        
        return source;
    }
    
    /**
     * Creates a CorsFilter bean directly for use outside of Spring Security.
     * This filter has highest precedence to ensure CORS headers are applied before other processing.
     * 
     * @return CorsFilter with highest precedence
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}

