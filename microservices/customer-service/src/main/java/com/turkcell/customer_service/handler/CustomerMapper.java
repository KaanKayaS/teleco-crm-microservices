package com.turkcell.customer_service.handler;

import com.turkcell.customer_service.dto.response.CustomerResponse;
import com.turkcell.customer_service.dto.response.DocumentResponse;
import com.turkcell.customer_service.entity.Customer;

import java.util.List;

/**
 * Shared entity → DTO mapper utilities used by all handlers.
 */
public final class CustomerMapper {

    private CustomerMapper() {}

    public static CustomerResponse toResponse(Customer c) {
        List<DocumentResponse> docs = c.getDocuments() == null ? List.of() :
                c.getDocuments().stream()
                        .map(d -> new DocumentResponse(
                                d.getId(),
                                c.getId(),
                                d.getType(),
                                d.getFileRef(),
                                d.getVerifiedAt()))
                        .toList();

        return new CustomerResponse(
                c.getId(),
                c.getUserId(),
                c.getFirstName(),
                c.getLastName(),
                c.getIdentityNumber(),
                c.getPhone(),
                c.getEmail(),
                c.getStatus(),
                c.isApproved(),
                c.getCreatedAt(),
                docs
        );
    }
}
