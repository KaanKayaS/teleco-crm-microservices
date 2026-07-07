package com.turkcell.orderservice.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void consumePaymentEvents(String event) {
        // Logic to consume PaymentCompleted, PaymentFailed
        System.out.println("Received payment event: " + event);
    }

    @KafkaListener(topics = "subscription-events", groupId = "order-service-group")
    public void consumeSubscriptionEvents(String event) {
        // Logic to consume SubscriptionActivated
        System.out.println("Received subscription event: " + event);
    }
}
