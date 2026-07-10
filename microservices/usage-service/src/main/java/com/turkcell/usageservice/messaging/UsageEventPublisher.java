package com.turkcell.usageservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.usageservice.model.dto.QuotaExceededEvent;
import com.turkcell.usageservice.model.dto.QuotaThresholdReachedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UsageEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String TOPIC = "usage-events";

    public UsageEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishThresholdReached(QuotaThresholdReachedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, event.getSubscriptionId().toString(), payload);
        } catch (Exception e) {
            // handle or log exception
        }
    }

    public void publishQuotaExceeded(QuotaExceededEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, event.getSubscriptionId().toString(), payload);
        } catch (Exception e) {
            // handle or log exception
        }
    }
}
