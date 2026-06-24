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
        if (!request.getMethod().equals("GET") || !request.getRequestURI().matches(".*/api/v1/customers/.*")) {
            return true; // We only secure GET /api/v1/customers/{id} specifically in this interceptor
        }

        String roles = request.getHeader("X-User-Roles");
        String userId = request.getHeader("X-User-Id");

        if (roles == null || roles.isEmpty() || roles.contains("anonymous")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Anonymous users cannot access customer data");
            return false;
        }

        if (roles.contains("ROLE_BACKOFFICE_STAFF")) {
            return true;
        }

        if (roles.contains("ROLE_SUBSCRIBER")) {
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
                        if (!customer.isApproved()) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Customer account is pending admin approval");
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
