package com.turkcell.customer_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JwtHeaderFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest) {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String[] parts = token.split("\\.");
                    if (parts.length >= 2) {
                        String payload = parts[1];
                        int missingPadding = (4 - payload.length() % 4) % 4;
                        if (missingPadding > 0) {
                            payload += "=".repeat(missingPadding);
                        }
                        String payloadJson = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
                        Map<String, Object> payloadMap = objectMapper.readValue(payloadJson, Map.class);
                        
                        String userId = (String) payloadMap.get("sub");
                        
                        List<String> rolesList = new ArrayList<>();
                        Map<String, Object> realmAccess = (Map<String, Object>) payloadMap.get("realm_access");
                        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
                            for (Object role : roles) {
                                rolesList.add("ROLE_" + role.toString().toUpperCase());
                            }
                        }
                        
                        String roles = String.join(",", rolesList);

                        // Wrap request to inject headers
                        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
                            @Override
                            public String getHeader(String name) {
                                if ("X-User-Id".equalsIgnoreCase(name)) {
                                    return userId;
                                }
                                if ("X-User-Roles".equalsIgnoreCase(name)) {
                                    return roles;
                                }
                                return super.getHeader(name);
                            }

                            @Override
                            public Enumeration<String> getHeaders(String name) {
                                if ("X-User-Id".equalsIgnoreCase(name)) {
                                    return Collections.enumeration(Collections.singletonList(userId));
                                }
                                if ("X-User-Roles".equalsIgnoreCase(name)) {
                                    return Collections.enumeration(Collections.singletonList(roles));
                                }
                                return super.getHeaders(name);
                            }

                            @Override
                            public Enumeration<String> getHeaderNames() {
                                Set<String> names = new LinkedHashSet<>();
                                names.add("X-User-Id");
                                names.add("X-User-Roles");
                                Enumeration<String> baseNames = super.getHeaderNames();
                                while (baseNames.hasMoreElements()) {
                                    names.add(baseNames.nextElement());
                                }
                                return Collections.enumeration(names);
                            }
                        };
                        
                        chain.doFilter(wrappedRequest, response);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        chain.doFilter(request, response);
    }
}
