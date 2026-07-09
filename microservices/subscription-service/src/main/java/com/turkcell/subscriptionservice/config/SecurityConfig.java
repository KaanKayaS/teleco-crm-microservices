package com.turkcell.subscriptionservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public: actuator, swagger, openapi docs
                        .requestMatchers(
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        // Internal creation endpoint — called by order-service (BACKOFFICE_STAFF)
                        .requestMatchers(HttpMethod.POST, "/api/v1/subscriptions")
                                .hasRole("BACKOFFICE_STAFF")
                        // Read — subscriber and backoffice can view
                        .requestMatchers(HttpMethod.GET, "/api/v1/subscriptions/**")
                                .hasAnyRole("SUBSCRIBER", "BACKOFFICE_STAFF")
                        // State transitions — BACKOFFICE_STAFF only
                        .requestMatchers(HttpMethod.POST, "/api/v1/subscriptions/*/suspend")
                                .hasRole("BACKOFFICE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/v1/subscriptions/*/reactivate")
                                .hasRole("BACKOFFICE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/v1/subscriptions/*/terminate")
                                .hasRole("BACKOFFICE_STAFF")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        org.springframework.core.convert.converter.Converter<
                org.springframework.security.oauth2.jwt.Jwt,
                Collection<org.springframework.security.core.GrantedAuthority>
        > grantedAuthoritiesConverter = jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return Collections.emptyList();
            }
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            return roles.stream()
                    .map(roleName -> "ROLE_" + roleName)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        };

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }
}
