package com.turkcell.identityservice.service;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    private Keycloak getAdminKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType("password")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    public String registerSubscriber(String username, String password, String email, String firstName, String lastName) {
        Keycloak keycloak = getAdminKeycloak();
        
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        user.setCredentials(Collections.singletonList(credential));

        Response response = keycloak.realm(realm).users().create(user);
        
        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak, status: " + response.getStatus());
        }

        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        RoleRepresentation subscriberRole = keycloak.realm(realm).roles().get("SUBSCRIBER").toRepresentation();
        keycloak.realm(realm).users().get(userId).roles().realmLevel().add(Collections.singletonList(subscriberRole));

        return userId;
    }

    public Map<String, String> login(String username, String password) {
        Keycloak userKeycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType("password")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .build();
        
        return Map.of("accessToken", userKeycloak.tokenManager().getAccessTokenString());
    }
}
