package com.turkcell.customer_service.entity.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerRegisteredEvent(
        UUID customerId,
        String firstName,
        String lastName,
        String identityNumber,
        String email,
        String phone,
        OffsetDateTime occurredAt
) {}
