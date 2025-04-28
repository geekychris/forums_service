package com.example.forum.cli.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AuthConfig {

    @Value("${forum.auth.token-file}")
    private String tokenFilePath;

    @Bean
    public ExchangeFilterFunction authorizationFilter() {
        return (request, next) -> {
            String token = readToken();
            if (token != null && !token.isEmpty()) {
                ClientRequest filtered = ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build();
                return next.exchange(filtered);
            }
            return next.exchange(request);
        };
    }

    private String readToken() {
        try {
            Path path = Paths.get(tokenFilePath);
            File tokenFile = path.toFile();
            if (tokenFile.exists()) {
                return Files.readString(path).trim();
            }
        } catch (IOException e) {
            // Token file doesn't exist or can't be read, which is fine for unauthenticated requests
        }
        return null;
    }
}

