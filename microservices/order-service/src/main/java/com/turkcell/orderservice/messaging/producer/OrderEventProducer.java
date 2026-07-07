package com.turkcell.orderservice.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.orderservice.model.entity.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrderCreated(Order order) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("orderId", order.getId().toString());
            event.put("customerId", order.getCustomerId());
            event.put("totalAmount", order.getTotalAmount());
            event.put("currency", order.getCurrency());
            
            kafkaTemplate.send("order-events", order.getId().toString(), objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            throw new RuntimeException("Error serializing order event", e);
        }
    }

    public void publishOrderCancelled(Order order) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("orderId", order.getId().toString());
            event.put("status", "CANCELLED");

            kafkaTemplate.send("order-events", order.getId().toString(), objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            throw new RuntimeException("Error serializing order event", e);
        }
    }
}
