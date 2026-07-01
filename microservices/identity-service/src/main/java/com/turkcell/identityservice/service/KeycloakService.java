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
                .realm("master")
                .grantType("password")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            Keycloak keycloak = getAdminKeycloak();
            List<UserRepresentation> users = keycloak.realm(realm).users().search("admin-staff");
            if (!users.isEmpty()) {
                UserRepresentation user = users.get(0);
                
                // Reset password to admin123 (non-temporary)
                CredentialRepresentation cred = new CredentialRepresentation();
                cred.setType(CredentialRepresentation.PASSWORD);
                cred.setValue("admin123");
                cred.setTemporary(false);
                keycloak.realm(realm).users().get(user.getId()).resetPassword(cred);
                
                // Clear required actions
                user.setRequiredActions(Collections.emptyList());
                keycloak.realm(realm).users().get(user.getId()).update(user);
                
                System.out.println("Successfully reset password and cleared required actions for admin-staff!");
            }
        } catch (Exception e) {
            System.err.println("Failed to update admin-staff credentials: " + e.getMessage());
            e.printStackTrace();
        }
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

    public Map<String, Object> login(String username, String password) {
        Keycloak userKeycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType("password")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .build();
        
        org.keycloak.representations.AccessTokenResponse tokenResponse = userKeycloak.tokenManager().getAccessToken();
        
        Map<String, Object> responseMap = new java.util.HashMap<>();
        responseMap.put("accessToken", tokenResponse.getToken() != null ? tokenResponse.getToken() : "");
        responseMap.put("refreshToken", tokenResponse.getRefreshToken() != null ? tokenResponse.getRefreshToken() : "");
        responseMap.put("expiresIn", tokenResponse.getExpiresIn());
        responseMap.put("refreshExpiresIn", tokenResponse.getRefreshExpiresIn());
        responseMap.put("tokenType", tokenResponse.getTokenType() != null ? tokenResponse.getTokenType() : "");
        return responseMap;
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            String formData = "grant_type=refresh_token" +
                    "&refresh_token=" + java.net.URLEncoder.encode(refreshToken, java.nio.charset.StandardCharsets.UTF_8) +
                    "&client_id=" + java.net.URLEncoder.encode(clientId, java.nio.charset.StandardCharsets.UTF_8) +
                    "&client_secret=" + java.net.URLEncoder.encode(clientSecret, java.nio.charset.StandardCharsets.UTF_8);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to refresh token, status: " + response.statusCode() + ", body: " + response.body());
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(response.body(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while refreshing token: " + e.getMessage(), e);
        }
    }
}
