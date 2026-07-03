package com.turkcell.customer_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.customer_service.entity.OutboxEvent;
import com.turkcell.customer_service.entity.event.CustomerKYCApprovedEvent;
import com.turkcell.customer_service.entity.event.CustomerRegisteredEvent;
import com.turkcell.customer_service.entity.event.CustomerUpdatedEvent;
import com.turkcell.customer_service.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class OutboxEventScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventScheduler.class);

    private final OutboxEventRepository outboxEventRepository;
    private final CustomerEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public OutboxEventScheduler(OutboxEventRepository outboxEventRepository, 
                                CustomerEventPublisher eventPublisher, 
                                ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-rate:5000}")
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatus("PENDING");

        for (OutboxEvent event : pendingEvents) {
            try {
                Object payload = convertPayload(event.getType(), event.getPayload());
                String topic = determineTopic(event.getType());

                eventPublisher.publishEvent(topic, event.getAggregateId(), payload)
                        .thenRun(() -> markAsProcessed(event))
                        .exceptionally(ex -> {
                            markAsFailed(event, ex);
                            return null;
                        });

            } catch (Exception e) {
                markAsFailed(event, e);
            }
        }
    }

    private Object convertPayload(String type, Object payloadMap) {
        return switch (type) {
            case "CustomerRegistered" -> objectMapper.convertValue(payloadMap, CustomerRegisteredEvent.class);
            case "CustomerKYCApproved" -> objectMapper.convertValue(payloadMap, CustomerKYCApprovedEvent.class);
            case "CustomerUpdated" -> objectMapper.convertValue(payloadMap, CustomerUpdatedEvent.class);
            default -> throw new IllegalArgumentException("Unknown event type: " + type);
        };
    }

    private String determineTopic(String type) {
        return switch (type) {
            case "CustomerRegistered" -> com.turkcell.customer_service.config.KafkaConfig.TOPIC_CUSTOMER_REGISTERED;
            case "CustomerKYCApproved" -> com.turkcell.customer_service.config.KafkaConfig.TOPIC_CUSTOMER_KYC_APPROVED;
            case "CustomerUpdated" -> com.turkcell.customer_service.config.KafkaConfig.TOPIC_CUSTOMER_UPDATED;
            default -> throw new IllegalArgumentException("Unknown event type: " + type);
        };
    }

    private void markAsProcessed(OutboxEvent event) {
        event.setStatus("PROCESSED");
        event.setProcessedAt(OffsetDateTime.now());
        outboxEventRepository.save(event);
        log.info("Successfully processed outbox event: {}", event.getId());
    }

    private void markAsFailed(OutboxEvent event, Throwable ex) {
        event.setStatus("FAILED");
        event.setProcessedAt(OffsetDateTime.now());
        outboxEventRepository.save(event);
        log.error("Failed to process outbox event: {}", event.getId(), ex);
    }
}
