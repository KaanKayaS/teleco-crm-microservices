package com.turkcell.customer_service.security;

import com.turkcell.customer_service.entity.Customer;
import com.turkcell.customer_service.repository.CustomerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.UUID;

@Component
public class CustomerSecurityInterceptor implements HandlerInterceptor {

    private final CustomerRepository customerRepository;

    public CustomerSecurityInterceptor(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        String roles = request.getHeader("X-User-Roles");
        String userId = request.getHeader("X-User-Id");

        if (roles == null || roles.isEmpty() || roles.contains("anonymous")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied: Unauthenticated");
            return false;
        }

        if (roles.contains("ROLE_BACKOFFICE_STAFF")) {
            return true;
        }

        if (roles.contains("ROLE_SUBSCRIBER")) {
            if (method.equals("POST") && uri.equals("/api/v1/customers")) {
                return true;
            }

            Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (pathVariables != null && pathVariables.containsKey("id")) {
                String customerIdStr = pathVariables.get("id");
                try {
                    UUID customerId = UUID.fromString(customerIdStr);
                    Customer customer = customerRepository.findById(customerId).orElse(null);

                    if (customer == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer not found");
                        return false;
                    }

                    if (userId != null && userId.equals(customer.getUserId())) {
                        if (uri.endsWith("/kyc/approve")) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Only staff can approve KYC");
                            return false;
                        }
                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID");
                    return false;
                }
            }
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Insufficient permissions");
        return false;
    }
}
