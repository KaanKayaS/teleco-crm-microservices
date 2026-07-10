package com.turkcell.billingservice.messaging.producer;
import com.turkcell.billingservice.model.entity.OutboxEvent;
import com.turkcell.billingservice.repository.OutboxEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class OutboxEventPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final BillingEventProducer producer;

    public OutboxEventPublisher(OutboxEventRepository outboxEventRepository, BillingEventProducer producer) {
        this.outboxEventRepository = outboxEventRepository;
        this.producer = producer;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        for (OutboxEvent event : events) {
            producer.publishEvent("billing-events", event.getPayload());
            event.setStatus("PROCESSED");
            event.setProcessedAt(ZonedDateTime.now());
            outboxEventRepository.save(event);
        }
    }
}
