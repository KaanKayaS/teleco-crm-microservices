package com.turkcell.productcatalog.scheduler;

import com.turkcell.productcatalog.messaging.producer.ProductEventProducer;
import com.turkcell.productcatalog.model.entity.OutboxEvent;
import com.turkcell.productcatalog.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ProductEventProducer productEventProducer;

    public OutboxScheduler(OutboxEventRepository outboxEventRepository, ProductEventProducer productEventProducer) {
        this.outboxEventRepository = outboxEventRepository;
        this.productEventProducer = productEventProducer;
    }

    @Scheduled(fixedDelayString = "5000") // Run every 5 seconds
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events. Processing...", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // Topic can be determined based on aggregate type or hardcoded
                String topic = "product-events"; 
                productEventProducer.sendEvent(topic, event.getType(), event.getAggregateId(), event.getPayload());
                
                event.setStatus("PROCESSED");
                event.setProcessedAt(ZonedDateTime.now());
                outboxEventRepository.save(event);
                
                log.info("Successfully processed event ID: {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to process event ID: {}", event.getId(), e);
                // Depending on requirement, we could set it to FAILED, or let it retry on next schedule.
            }
        }
    }
}
