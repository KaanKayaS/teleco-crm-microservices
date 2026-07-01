package com.turkcell.customer_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    @Bean
    public OperationCustomizer addGlobalHeaders() {
        return (operation, handlerMethod) -> {
            boolean hasUserId = operation.getParameters() != null && operation.getParameters().stream()
                    .anyMatch(p -> "X-User-Id".equalsIgnoreCase(p.getName()));
            boolean hasUserRoles = operation.getParameters() != null && operation.getParameters().stream()
                    .anyMatch(p -> "X-User-Roles".equalsIgnoreCase(p.getName()));

            if (!hasUserId) {
                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-User-Id")
                        .description("Keycloak User ID (sub claim) - Leave empty if using Bearer token")
                        .required(false));
            }

            if (!hasUserRoles) {
                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-User-Roles")
                        .description("User roles (e.g. ROLE_SUBSCRIBER) - Leave empty if using Bearer token")
                        .required(false));
            }

            return operation;
        };
    }
}
