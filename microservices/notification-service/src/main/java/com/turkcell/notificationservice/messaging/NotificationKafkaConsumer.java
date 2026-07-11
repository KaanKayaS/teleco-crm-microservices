package com.turkcell.notificationservice.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    private String extractCustomerId(JsonNode node) {
        if (node.has("payload") && node.get("payload").has("customerId")) {
            return node.get("payload").get("customerId").asText();
        } else if (node.has("customerId")) {
            return node.get("customerId").asText();
        }
        return null;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service-group-5")
    public void consumeOrderEvent(String payload) {
        try {
            log.info("Received order-events payload: {}", payload);
            JsonNode node = objectMapper.readTree(payload);
            String customerId = extractCustomerId(node);
            
            if (customerId != null) {
                notificationService.processEventAndNotify("ORDER_CONFIRMED", UUID.fromString(customerId), payload);
            }
        } catch (Exception e) {
            log.error("Error processing order-events: ", e);
        }
    }

    @KafkaListener(topics = "subscription-events", groupId = "notification-service-group-5")
    public void consumeSubscriptionEvent(String payload) {
        try {
            log.info("Received subscription-events payload: {}", payload);
            JsonNode node = objectMapper.readTree(payload);
            String customerId = extractCustomerId(node);
            
            if (customerId != null) {
                notificationService.processEventAndNotify("SUBSCRIPTION_ACTIVATED", UUID.fromString(customerId), payload);
            }
        } catch (Exception e) {
            log.error("Error processing subscription-events: ", e);
        }
    }

    @KafkaListener(topics = "billing-events", groupId = "notification-service-group-5")
    public void consumeInvoiceEvent(String payload) {
        try {
            log.info("Received invoice-generated-events payload: {}", payload);
            JsonNode node = objectMapper.readTree(payload);
            String customerId = extractCustomerId(node);
            if (customerId != null) {
                notificationService.processEventAndNotify("INVOICE_GENERATED", UUID.fromString(customerId), payload);
            }
        } catch (Exception e) {
            log.error("Error processing invoice-generated-events: ", e);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group-5")
    public void consumePaymentEvent(String payload) {
        try {
            log.info("Received payment-events payload: {}", payload);
            JsonNode node = objectMapper.readTree(payload);
            String customerId = extractCustomerId(node);
            if (customerId != null) {
                String eventType = node.has("eventType") ? node.get("eventType").asText() : "";
                String template = "PaymentRefunded".equals(eventType) ? "PAYMENT_REFUNDED" : "PAYMENT_SUCCESS";
                notificationService.processEventAndNotify(template, UUID.fromString(customerId), payload);
            }
        } catch (Exception e) {
            log.error("Error processing payment-events: ", e);
        }
    }
}
