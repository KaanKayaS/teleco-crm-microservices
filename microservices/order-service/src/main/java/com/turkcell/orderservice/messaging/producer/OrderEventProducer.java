package com.turkcell.orderservice.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.orderservice.model.entity.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrderCreated(Order order) {
        Map<String, Object> event = new HashMap<>();
        event.put("orderId", order.getId().toString());
        event.put("customerId", order.getCustomerId());
        event.put("totalAmount", order.getTotalAmount());
        event.put("currency", order.getCurrency());
        
        kafkaTemplate.send("order-events", order.getId().toString(), event);
    }

    public void publishOrderCancelled(Order order) {
        Map<String, Object> event = new HashMap<>();
        event.put("orderId", order.getId().toString());
        event.put("status", "CANCELLED");

        kafkaTemplate.send("order-events", order.getId().toString(), event);
    }
}
