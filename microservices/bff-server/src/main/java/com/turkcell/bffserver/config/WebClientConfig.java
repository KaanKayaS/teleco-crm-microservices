package com.turkcell.bffserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configured to call the API Gateway.
 * The Authorization header (Bearer JWT) is passed by each controller
 * from the incoming request — this config just sets the base URL.
 */
@Configuration
public class WebClientConfig {

    @Value("${gateway.base-url:http://localhost:9000}")
    private String gatewayBaseUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(gatewayBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
