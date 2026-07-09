package com.turkcell.subscriptionservice.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.subscriptionservice.model.entity.Subscription;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Publishes subscription lifecycle events to the "subscription-events" Kafka topic.
 *
 * Events published:
 *  - SubscriptionActivated
 *  - SubscriptionSuspended
 *  - SubscriptionTerminated
 */
@Component
public class SubscriptionEventProducer {

    private static final String TOPIC = "subscription-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SubscriptionEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                                     ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSubscriptionActivated(Subscription subscription) {
        publishEvent("SubscriptionActivated", subscription);
    }

    public void publishSubscriptionSuspended(Subscription subscription) {
        publishEvent("SubscriptionSuspended", subscription);
    }

    public void publishSubscriptionTerminated(Subscription subscription) {
        publishEvent("SubscriptionTerminated", subscription);
    }

    private void publishEvent(String eventType, Subscription subscription) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("subscriptionId", subscription.getId().toString());
            event.put("customerId", subscription.getCustomerId().toString());
            event.put("msisdn", subscription.getMsisdn());
            event.put("tariffCode", subscription.getTariffCode());
            event.put("status", subscription.getStatus().name());

            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, subscription.getId().toString(), payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish " + eventType + " event", e);
        }
    }
}
