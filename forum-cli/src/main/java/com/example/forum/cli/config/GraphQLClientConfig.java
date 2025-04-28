package com.example.forum.cli.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GraphQLClientConfig {

    @Value("${forum.api.graphql-endpoint}")
    private String graphqlEndpoint;

    @Bean
    public HttpGraphQlClient graphQlClient(WebClient webClient) {
        return HttpGraphQlClient.builder(webClient)
                .url(graphqlEndpoint)
                .build();
    }
}

