package com.turkcell.bffserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Auth BFF Controller
 * Proxies login/register requests to Identity Service via API Gateway.
 * These endpoints are PUBLIC (no JWT required).
 *
 * Angular → POST /bff/auth/login → Gateway :9000 → Identity Service :9001
 */
@RestController
@RequestMapping("/bff/auth")
@Tag(name = "Auth (BFF)", description = "Kimlik doğrulama — Identity Service proxy")
public class AuthBffController {

    private final WebClient webClient;

    public AuthBffController(WebClient webClient) {
        this.webClient = webClient;
    }

    @Operation(summary = "Kullanıcı Girişi", description = "Keycloak JWT token alır. Response'daki accessToken'ı localStorage'a kaydet.")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = webClient.post()
                .uri("/api/v1/auth/login")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Kullanıcı Kaydı", description = "Keycloak'ta SUBSCRIBER rolüyle yeni kullanıcı oluşturur.")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = webClient.post()
                .uri("/api/v1/auth/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return ResponseEntity.ok(response);
    }
}
