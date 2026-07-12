package com.turkcell.ticketservice.messaging.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Low-level Kafka producer — sends raw JSON string payloads to a given topic.
 * Called exclusively by OutboxEventPublisher to ensure at-least-once delivery.
 */
@Service
public class TicketEventProducer {

    private static final String TICKET_EVENTS_TOPIC = "ticket-events";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public TicketEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEvent(String payload) {
        kafkaTemplate.send(TICKET_EVENTS_TOPIC, payload);
    }
}
