package com.turkcell.ticketservice.service;

import com.turkcell.ticketservice.model.entity.TicketPriority;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

/**
 * Computes SLA deadlines based on ticket priority.
 * FR-32: Ticket otomatik olarak ilgili ekibe SLA bazlı atanır.
 *
 * SLA rules:
 *   CRITICAL → 2  hours
 *   HIGH     → 8  hours
 *   MEDIUM   → 24 hours
 *   LOW      → 72 hours
 */
@Service
public class SlaService {

    public ZonedDateTime computeSlaDueAt(TicketPriority priority) {
        ZonedDateTime now = ZonedDateTime.now();
        return switch (priority) {
            case CRITICAL -> now.plusHours(2);
            case HIGH     -> now.plusHours(8);
            case MEDIUM   -> now.plusHours(24);
            case LOW      -> now.plusHours(72);
        };
    }
}
