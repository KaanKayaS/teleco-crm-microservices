package com.turkcell.ticketservice.messaging.producer;

import com.turkcell.ticketservice.model.entity.OutboxEvent;
import com.turkcell.ticketservice.repository.OutboxEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Transactional Outbox pattern publisher.
 *
 * Every 5 seconds, fetches all PENDING outbox events saved by TicketService
 * and reliably forwards them to Kafka. Marks events as PROCESSED or FAILED.
 *
 * This guarantees at-least-once delivery even if the app crashes after DB commit
 * but before Kafka send.
 */
@Component
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final TicketEventProducer producer;

    public OutboxEventPublisher(OutboxEventRepository outboxEventRepository,
                                TicketEventProducer producer) {
        this.outboxEventRepository = outboxEventRepository;
        this.producer = producer;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        for (OutboxEvent event : events) {
            try {
                producer.publishEvent(event.getPayload());
                event.setStatus("PROCESSED");
                event.setProcessedAt(ZonedDateTime.now());
            } catch (Exception e) {
                event.setStatus("FAILED");
                System.err.printf("[OUTBOX] Kafka publish başarısız — eventId=%s hata=%s%n",
                        event.getId(), e.getMessage());
            }
            outboxEventRepository.save(event);
        }
    }
}
