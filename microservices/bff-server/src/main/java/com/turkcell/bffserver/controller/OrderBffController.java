package com.turkcell.bffserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Order BFF Controller
 * Proxies order creation and payment processing.
 */
@RestController
@RequestMapping("/bff")
@Tag(name = "Order & Payment (BFF)", description = "Sipariş ve ödeme akışları")
@SecurityRequirement(name = "bearerAuth")
public class OrderBffController {

    private final WebClient webClient;

    public OrderBffController(WebClient webClient) {
        this.webClient = webClient;
    }

    @Operation(summary = "Sipariş Oluştur", description = "Order Service'e yeni sipariş gönderir.")
    @PostMapping("/orders")
    public ResponseEntity<Map> createOrder(@AuthenticationPrincipal Jwt jwt,
                                           @RequestBody Map<String, Object> request) {
        Map response = webClient.post()
                .uri("/api/v1/orders")
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ödeme Al", description = "Payment Service'e mock ödeme gönderir. Sipariş statüsünü değiştirir.")
    @PostMapping("/payments")
    public ResponseEntity<Map> processPayment(@AuthenticationPrincipal Jwt jwt,
                                              @RequestBody Map<String, Object> request) {
        Map response = webClient.post()
                .uri("/api/v1/payments")
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return ResponseEntity.ok(response);
    }
}
