package com.example.forum.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 * Provides centralized configuration for API metadata, security schemes, and API grouping.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates the OpenAPI documentation configuration.
     *
     * @return the OpenAPI configuration
     */
    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Forum API")
                        .description("Complete API documentation for the Forum application")
                        .version("1.0.0")
                        .termsOfService("https://example.com/terms")
                        .contact(new Contact()
                                .name("Forum Team")
                                .email("support@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Forum API Documentation")
                        .url("https://example.com/docs"))
                .addServersItem(new Server()
                        .url("/")
                        .description("Default Server URL"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("JWT Authentication. Enter 'Bearer ' followed by your token")));
    }
    
    /**
     * Configuration for grouping admin-related endpoints.
     * 
     * @return GroupedOpenApi for admin endpoints
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/admin/**")
                .build();
    }
    
    /**
     * Configuration for grouping authentication-related endpoints.
     * 
     * @return GroupedOpenApi for auth endpoints
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/auth/**")
                .build();
    }
    
    /**
     * Configuration for grouping user-related endpoints.
     * 
     * @return GroupedOpenApi for user endpoints
     */
    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/api/users/**")
                .build();
    }
    
    /**
     * Configuration for grouping forum-related endpoints.
     * 
     * @return GroupedOpenApi for forum endpoints
     */
    @Bean
    public GroupedOpenApi forumsApi() {
        return GroupedOpenApi.builder()
                .group("forums")
                .pathsToMatch("/api/forums/**")
                .build();
    }
    
    /**
     * Configuration for grouping post-related endpoints.
     * 
     * @return GroupedOpenApi for post endpoints
     */
    @Bean
    public GroupedOpenApi postsApi() {
        return GroupedOpenApi.builder()
                .group("posts")
                .pathsToMatch("/api/posts/**")
                .build();
    }
    
    /**
     * Configuration for grouping comment-related endpoints.
     * 
     * @return GroupedOpenApi for comment endpoints
     */
    @Bean
    public GroupedOpenApi commentsApi() {
        return GroupedOpenApi.builder()
                .group("comments")
                .pathsToMatch("/api/comments/**")
                .build();
    }
    
    /**
     * Default group for any endpoints not explicitly included in other groups.
     * 
     * @return GroupedOpenApi for all remaining endpoints
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/api/**")
                .build();
    }
}
