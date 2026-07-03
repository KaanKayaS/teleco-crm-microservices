package com.turkcell.customer_service.entity.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerUpdatedEvent(
        UUID customerId,
        String firstName,
        String lastName,
        String email,
        String phone,
        OffsetDateTime occurredAt
) {}
