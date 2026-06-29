package com.turkcell.customer_service.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String userId,
        String firstName,
        String lastName,
        String identityNumber,
        String phone,
        String email,
        String status,
        boolean isApproved,
        OffsetDateTime createdAt,
        List<DocumentResponse> documents
) {}
