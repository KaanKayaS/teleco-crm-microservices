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
 * Catalog BFF Controller
 * Proxies tariff and addon listing from Product Catalog Service.
 */
@RestController
@RequestMapping("/bff/catalog")
@Tag(name = "Catalog (BFF)", description = "Ürün kataloğu — Tarife ve Ek Paket listesi")
@SecurityRequirement(name = "bearerAuth")
public class CatalogBffController {

    private final WebClient webClient;

    public CatalogBffController(WebClient webClient) {
        this.webClient = webClient;
    }

    @Operation(summary = "Tüm Tarifeleri Listele", description = "Aktif postpaid ve prepaid tarifeleri döner.")
    @GetMapping("/tariffs")
    public ResponseEntity<List<Map>> getTariffs(@AuthenticationPrincipal Jwt jwt) {
        List<Map> tariffs = webClient.get()
                .uri("/api/v1/tariffs")
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .block();
        return ResponseEntity.ok(tariffs);
    }

    @Operation(summary = "Tarife Detayı", description = "Belirli bir tarifeyi code'a göre döner.")
    @GetMapping("/tariffs/{code}")
    public ResponseEntity<Map> getTariff(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable String code) {
        Map tariff = webClient.get()
                .uri("/api/v1/tariffs/" + code)
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return ResponseEntity.ok(tariff);
    }

    @Operation(summary = "Tüm Ek Paketleri Listele", description = "Mevcut addon'ları listeler.")
    @GetMapping("/addons")
    public ResponseEntity<List<Map>> getAddons(@AuthenticationPrincipal Jwt jwt) {
        List<Map> addons = webClient.get()
                .uri("/api/v1/addons")
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .block();
        return ResponseEntity.ok(addons);
    }
}
