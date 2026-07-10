package com.turkcell.usageservice.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.usageservice.service.UsageService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SubscriptionEventConsumer {

    private final UsageService usageService;
    private final ObjectMapper objectMapper;

    public SubscriptionEventConsumer(UsageService usageService, ObjectMapper objectMapper) {
        this.usageService = usageService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "subscription-events", groupId = "usage-service-group-2")
    public void consumeSubscriptionEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();

            if ("SubscriptionActivated".equals(eventType)) {
                String subscriptionIdStr = event.get("subscriptionId").asText();
                UUID subscriptionId = UUID.fromString(subscriptionIdStr);
                usageService.initializeQuotaForSubscription(subscriptionId);
            }
        } catch (Exception e) {
            System.err.println("Error processing subscription event: " + message);
            e.printStackTrace();
        }
    }
}
