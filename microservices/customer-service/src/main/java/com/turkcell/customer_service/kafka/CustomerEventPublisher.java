package com.turkcell.customer_service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CustomerEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CustomerEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<Void> publishEvent(String topic, String key, Object eventPayload) {
        return kafkaTemplate.send(topic, key, eventPayload)
                .thenApply(result -> null); // success
    }
}
