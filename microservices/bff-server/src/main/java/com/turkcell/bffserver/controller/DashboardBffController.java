package com.turkcell.bffserver.controller;

import com.turkcell.bffserver.dto.DashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Dashboard BFF Controller — Aggregator Pattern
 *
 * Single endpoint that calls 4 microservices IN PARALLEL via reactive WebClient
 * and returns a combined DashboardResponse to Angular.
 *
 * Flow: Angular GET /bff/dashboard
 *   ├── GET :9000/api/v1/customers/{customerId}
 *   ├── GET :9000/api/v1/subscriptions/{subscriptionId}
 *   ├── GET :9000/api/v1/usage/{subscriptionId}
 *   └── GET :9000/api/v1/invoices?customerId={id}&size=3
 */
@RestController
@RequestMapping("/bff")
@Tag(name = "Dashboard (BFF)", description = "Dashboard aggregator — 4 servisi tek istekte birleştirir")
@SecurityRequirement(name = "bearerAuth")
public class DashboardBffController {

    private final WebClient webClient;

    public DashboardBffController(WebClient webClient) {
        this.webClient = webClient;
    }

    @Operation(
        summary = "Dashboard Verilerini Getir",
        description = "Customer + Subscription + Usage + Son 3 Fatura bilgilerini tek seferde döner."
    )
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String subscriptionId) {

        String authHeader = "Bearer " + jwt.getTokenValue();

        // Parallel reactive calls
        Mono<Map> customerMono = fetchSafe(authHeader,
                "/api/v1/customers/" + (customerId != null ? customerId : jwt.getSubject()));

        Mono<Map> subscriptionMono = subscriptionId != null
                ? fetchSafe(authHeader, "/api/v1/subscriptions/" + subscriptionId)
                : Mono.just(Map.of("message", "subscriptionId parametresi gerekli"));

        Mono<Map> usageMono = subscriptionId != null
                ? fetchSafe(authHeader, "/api/v1/usage/" + subscriptionId)
                : Mono.just(Map.of("message", "subscriptionId parametresi gerekli"));

        Mono<List> invoicesMono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/invoices")
                        .queryParam("customerId", customerId != null ? customerId : jwt.getSubject())
                        .queryParam("size", 3)
                        .build())
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToMono(List.class)
                .onErrorReturn(WebClientResponseException.class, List.of());

        // Combine all 4 in parallel
        DashboardResponse response = Mono.zip(customerMono, subscriptionMono, usageMono, invoicesMono)
                .map(tuple -> new DashboardResponse(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4()
                ))
                .block();

        return ResponseEntity.ok(response);
    }

    private Mono<Map> fetchSafe(String authHeader, String path) {
        return webClient.get()
                .uri(path)
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorReturn(WebClientResponseException.class, Map.of("error", "Servis yanıt vermedi: " + path));
    }
}
