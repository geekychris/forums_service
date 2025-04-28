package com.example.forum.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Test configuration for integration tests
 */
@TestConfiguration
@PropertySource("classpath:application-test.properties")
@Slf4j
@ActiveProfiles("test")
public class IntegrationTestConfig {

    /**
     * Create a WebClient for testing with a mock base URL
     */
    @Bean
    @Primary
    public WebClient testWebClient() {
        // Configure HTTP client with minimal timeouts for testing
        HttpClient httpClient = HttpClient.create()
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .responseTimeout(Duration.ofMillis(3000))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(3000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(3000, TimeUnit.MILLISECONDS)));

        // Build the WebClient with all configurations
        return WebClient.builder()
                .baseUrl("http://localhost:8080") // Test base URL
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    /**
     * Create a filter function for logging request details
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.debug("Test Request: {} {}", request.method(), request.url());
            return Mono.just(request);
        });
    }

    /**
     * Create a filter function for logging response details
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.debug("Test Response status: {}", response.statusCode());
            return Mono.just(response);
        });
    }
}

