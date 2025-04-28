package com.example.forum.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main application class for the Forum CLI client.
 * This CLI tool provides a command-line interface to interact with the Forum API.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ForumCliApplication {
    /**
     * Main entry point for the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ForumCliApplication.class, args);
    }
}
