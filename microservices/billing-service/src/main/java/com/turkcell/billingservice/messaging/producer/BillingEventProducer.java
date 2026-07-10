package com.turkcell.billingservice.messaging.producer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BillingEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    public BillingEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void publishEvent(String topic, String payload) {
        kafkaTemplate.send(topic, payload);
    }
}
