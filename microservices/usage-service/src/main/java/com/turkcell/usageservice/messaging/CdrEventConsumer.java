package com.turkcell.usageservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.usageservice.model.dto.CdrRecordedEvent;
import com.turkcell.usageservice.service.UsageService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CdrEventConsumer {

    private final UsageService usageService;
    private final ObjectMapper objectMapper;

    public CdrEventConsumer(UsageService usageService, ObjectMapper objectMapper) {
        this.usageService = usageService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "cdr-events", groupId = "usage-service-group-2")
    public void consumeCdrEvent(String message) {
        try {
            CdrRecordedEvent event = objectMapper.readValue(message, CdrRecordedEvent.class);
            usageService.processCdr(event);
        } catch (Exception e) {
            System.err.println("Error processing CDR event: " + message);
            e.printStackTrace();
        }
    }
}
