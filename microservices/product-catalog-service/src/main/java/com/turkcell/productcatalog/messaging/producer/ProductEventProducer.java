package com.turkcell.productcatalog.messaging.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ProductEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(String topic, String eventType, String key, String payload) {
        log.info("Sending {} event to topic {} with key {}: {}", eventType, topic, key, payload);
        // Note: we can use spring.json.add.type.headers to handle types correctly, or just send JSON payload.
        kafkaTemplate.send(topic, key, payload);
    }
}
