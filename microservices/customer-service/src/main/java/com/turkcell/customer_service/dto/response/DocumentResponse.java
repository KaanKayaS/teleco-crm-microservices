package com.turkcell.customer_service.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID customerId,
        String type,
        String fileRef,
        OffsetDateTime verifiedAt
) {}
