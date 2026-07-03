package com.turkcell.customer_service.entity.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerKYCApprovedEvent(
        UUID customerId,
        String identityNumber,
        OffsetDateTime occurredAt
) {}
