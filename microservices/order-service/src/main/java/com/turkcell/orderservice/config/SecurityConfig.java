package com.turkcell.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**", "/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders").hasAnyRole("CUSTOMER", "BACKOFFICE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders/**").hasAnyRole("CUSTOMER", "BACKOFFICE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders/*/cancel").hasAnyRole("CUSTOMER", "BACKOFFICE_STAFF")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                );
        return http.build();
    }
}
