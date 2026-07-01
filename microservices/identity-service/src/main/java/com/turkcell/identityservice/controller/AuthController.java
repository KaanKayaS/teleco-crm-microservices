package com.turkcell.identityservice.controller;

import com.turkcell.identityservice.dto.LoginRequest;
import com.turkcell.identityservice.dto.RegisterRequest;
import com.turkcell.identityservice.service.KeycloakService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Kimlik doğrulama ve kullanıcı kayıt işlemleri (Keycloak)")
public class AuthController {

    private final KeycloakService keycloakService;

    public AuthController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @Operation(summary = "Kullanıcı Kaydı (Register)", description = "Keycloak üzerinde 'Subscriber' rolü ile yeni bir kullanıcı oluşturur.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kullanıcı başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Kayıt hatası veya geçersiz veri", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        String userId = keycloakService.registerSubscriber(
                request.username(), 
                request.password(), 
                request.email(), 
                request.firstName(), 
                request.lastName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("userId", userId));
    }

    @Operation(summary = "Kullanıcı Girişi (Login)", description = "Kullanıcı adı ve şifre ile giriş yapar, JWT Access Token ve Refresh Token döner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Giriş başarılı, tokenlar döndürüldü"),
            @ApiResponse(responseCode = "401", description = "Hatalı kullanıcı adı veya şifre", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> token = keycloakService.login(request.username(), request.password());
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Token Yenileme (Refresh Token)", description = "Mevcut refresh token'ı kullanarak yeni bir access token ve refresh token alır.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token başarıyla yenilendi"),
            @ApiResponse(responseCode = "400", description = "Geçersiz refresh token veya istek", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@RequestParam String refreshToken) {
        Map<String, Object> token = keycloakService.refreshToken(refreshToken);
        return ResponseEntity.ok(token);
    }
}
