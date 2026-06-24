package com.turkcell.identityservice.controller;

import com.turkcell.identityservice.dto.LoginRequest;
import com.turkcell.identityservice.dto.RegisterRequest;
import com.turkcell.identityservice.service.KeycloakService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final KeycloakService keycloakService;

    public AuthController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

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

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Map<String, String> token = keycloakService.login(request.username(), request.password());
        return ResponseEntity.ok(token);
    }
}
