package com.example.forum.cli.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for WebClient used in the forum CLI application.
 */
@Configuration("cliWebClientConfig")
@Slf4j
public class WebClientConfig {

    @Value("${forum.api.base-url}")
    private String baseUrl;

    @Value("${forum.api.rest-connect-timeout:5000}")
    private int connectTimeout;

    @Value("${forum.api.rest-read-timeout:15000}")
    private int readTimeout;

    /**
     * Creates and configures a WebClient bean for use in the application.
     *
     * @return a configured WebClient instance
     */
    @Bean("cliWebClient")
    public WebClient webClient() {
        log.info("Configuring WebClient with base URL: {}", baseUrl);
        
        // Configure HTTP client with timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS)));
        
        // Configure exchange strategies with increased memory buffer for larger responses
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer
                .build();

        // Build the WebClient with all configurations
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    /**
     * Creates a filter function for logging request details.
     *
     * @return an ExchangeFilterFunction for logging requests
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            if (log.isDebugEnabled()) {
                log.debug("Request: {} {}", request.method(), request.url());
                request.headers().forEach((name, values) -> 
                    values.forEach(value -> log.debug("{}={}", name, value))
                );
            }
            return Mono.just(request);
        });
    }

    /**
     * Creates a filter function for logging response details.
     *
     * @return an ExchangeFilterFunction for logging responses
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (log.isDebugEnabled()) {
                log.debug("Response status: {}", response.statusCode());
                response.headers().asHttpHeaders().forEach((name, values) -> 
                    values.forEach(value -> log.debug("{}={}", name, value))
                );
            }
            return Mono.just(response);
        });
    }
}

