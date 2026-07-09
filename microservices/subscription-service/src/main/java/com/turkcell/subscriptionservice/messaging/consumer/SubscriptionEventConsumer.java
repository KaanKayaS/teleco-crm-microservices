package com.turkcell.subscriptionservice.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.subscriptionservice.dto.CreateSubscriptionRequest;
import com.turkcell.subscriptionservice.service.SubscriptionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Consumes events from other services:
 *
 *  - OrderConfirmed  (order-events topic) → triggers subscription creation
 *  - PaymentFailed   (payment-events topic) → triggers subscription termination after grace period
 */
@Component
public class SubscriptionEventConsumer {

    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    public SubscriptionEventConsumer(SubscriptionService subscriptionService,
                                     ObjectMapper objectMapper) {
        this.subscriptionService = subscriptionService;
        this.objectMapper = objectMapper;
    }

    /**
     * Listens for OrderConfirmed events.
     * When an order is confirmed, a new subscription is automatically provisioned.
     */
    @KafkaListener(topics = "order-events", groupId = "subscription-service-group")
    public void consumeOrderEvents(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.path("eventType").asText("");

            if ("OrderConfirmed".equals(eventType)) {
                CreateSubscriptionRequest request = new CreateSubscriptionRequest();
                request.setCustomerId(UUID.fromString(node.path("customerId").asText()));
                request.setTariffCode(node.path("tariffCode").asText("STANDARD"));

                if (node.has("orderId")) {
                    request.setOrderId(UUID.fromString(node.path("orderId").asText()));
                }

                subscriptionService.createSubscription(request);
                System.out.println("[subscription-service] Subscription created from OrderConfirmed event.");
            }
        } catch (Exception e) {
            System.err.println("[subscription-service] Error processing order event: " + e.getMessage());
        }
    }

    /**
     * Listens for PaymentFailed events.
     * After a grace period (handled upstream), terminates the subscription.
     */
    @KafkaListener(topics = "payment-events", groupId = "subscription-service-group")
    public void consumePaymentEvents(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.path("eventType").asText("");

            if ("PaymentFailed".equals(eventType)) {
                String subscriptionIdStr = node.path("subscriptionId").asText(null);
                if (subscriptionIdStr != null) {
                    UUID subscriptionId = UUID.fromString(subscriptionIdStr);
                    subscriptionService.terminateSubscription(subscriptionId);
                    System.out.println("[subscription-service] Subscription terminated due to PaymentFailed: " + subscriptionId);
                }
            }
        } catch (Exception e) {
            System.err.println("[subscription-service] Error processing payment event: " + e.getMessage());
        }
    }
}
