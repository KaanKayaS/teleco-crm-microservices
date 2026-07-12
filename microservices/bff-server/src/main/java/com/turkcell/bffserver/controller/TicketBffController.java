package com.turkcell.bffserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Ticket BFF Controller
 * Proxies ticket creation and listing to Ticket Service.
 */
@RestController
@RequestMapping("/bff/tickets")
@Tag(name = "Tickets (BFF)", description = "Destek talepleri (Tickets)")
@SecurityRequirement(name = "bearerAuth")
public class TicketBffController {

    private final WebClient webClient;

    public TicketBffController(WebClient webClient) {
        this.webClient = webClient;
    }

    @Operation(summary = "Müşterinin Ticketlarını Getir", description = "Müşteriye ait tüm destek taleplerini listeler.")
    @GetMapping
    public ResponseEntity<List<Map>> getTickets(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam(required = false) String customerId) {
        String targetCustomerId = customerId != null ? customerId : jwt.getSubject();
        List<Map> tickets = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/tickets")
                        .queryParam("customerId", targetCustomerId)
                        .build())
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .block();
        return ResponseEntity.ok(tickets);
    }

    @Operation(summary = "Yeni Ticket Oluştur", description = "Yeni bir destek/arıza talebi açar.")
    @PostMapping
    public ResponseEntity<Map> createTicket(@AuthenticationPrincipal Jwt jwt,
                                            @RequestBody Map<String, Object> request) {
        // Automatically inject customerId from token if not present
        if (!request.containsKey("customerId")) {
            request.put("customerId", jwt.getSubject());
        }

        Map response = webClient.post()
                .uri("/api/v1/tickets")
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ticket Detayı Getir", description = "Yorumlarıyla birlikte ticket detayı.")
    @GetMapping("/{id}")
    public ResponseEntity<Map> getTicketDetail(@AuthenticationPrincipal Jwt jwt,
                                               @PathVariable String id) {
        Map response = webClient.get()
                .uri("/api/v1/tickets/" + id)
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Yorum Ekle", description = "Ticket'a yorum/cevap yazar.")
    @PostMapping("/{id}/comments")
    public ResponseEntity<Map> addComment(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable String id,
                                          @RequestBody Map<String, Object> request) {
        if (!request.containsKey("authorId")) {
            request.put("authorId", jwt.getSubject());
        }

        Map response = webClient.post()
                .uri("/api/v1/tickets/" + id + "/comments")
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return ResponseEntity.ok(response);
    }
}
