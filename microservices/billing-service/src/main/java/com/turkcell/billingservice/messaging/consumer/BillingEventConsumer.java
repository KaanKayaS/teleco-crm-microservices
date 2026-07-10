package com.turkcell.billingservice.messaging.consumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BillingEventConsumer {

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private final com.turkcell.billingservice.service.BillingService billingService;

    public BillingEventConsumer(com.turkcell.billingservice.service.BillingService billingService) {
        this.billingService = billingService;
    }

    @KafkaListener(topics = "subscription-events", groupId = "billing-service-group")
    public void handleSubscriptionEvents(String payload) {
        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(payload);
            String eventType = node.path("eventType").asText();
            if ("SubscriptionActivated".equals(eventType)) {
                java.util.UUID customerId = java.util.UUID.fromString(node.path("customerId").asText());
                java.util.UUID subscriptionId = java.util.UUID.fromString(node.path("subscriptionId").asText());
                String tariffCode = node.path("tariffCode").asText();
                billingService.handleSubscriptionActivated(customerId, subscriptionId, tariffCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @KafkaListener(topics = "usage-events", groupId = "billing-service-group")
    public void handleUsageEvents(String payload) {
        System.out.println("Received usage event in billing: " + payload);
    }

    @KafkaListener(topics = "payment-events", groupId = "billing-service-group")
    public void handlePaymentEvents(String payload) {
        System.out.println("Received payment event in billing: " + payload);
    }
}
