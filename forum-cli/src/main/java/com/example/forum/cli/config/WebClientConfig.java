package com.example.forum.cli.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${forum.api.base-url}")
    private String baseUrl;

    @Value("${forum.api.rest-connect-timeout:5000}")
    private int connectTimeout;

    @Value("${forum.api.rest-read-timeout:15000}")
    private int readTimeout;

    @Bean
    public WebClient webClient(ExchangeFilterFunction authorizationFilter) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(readTimeout))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(authorizationFilter)
                .build();
    }
}
