package com.turkcell.gatewayserver.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
public class TokenRelayGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(authentication -> {
                    String userId = "anonymous";
                    String roles = "anonymous";

                    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                        Jwt jwt = jwtAuth.getToken();
                        userId = jwt.getSubject();
                        roles = jwtAuth.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(","));
                    }

                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Roles", roles)
                            .build();

                    return exchange.mutate().request(mutatedRequest).build();
                })
                .defaultIfEmpty(exchange.mutate().request(
                        exchange.getRequest().mutate()
                                .header("X-User-Id", "anonymous")
                                .header("X-User-Roles", "anonymous")
                                .build()
                ).build())
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
