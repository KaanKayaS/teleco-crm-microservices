package com.turkcell.orderservice.service;

import com.turkcell.orderservice.model.entity.Order;
import com.turkcell.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final SagaOrchestrator sagaOrchestrator;

    public OrderService(OrderRepository orderRepository, SagaOrchestrator sagaOrchestrator) {
        this.orderRepository = orderRepository;
        this.sagaOrchestrator = sagaOrchestrator;
    }

    @Transactional
    public Order createOrder(Order order) {
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);
        sagaOrchestrator.startSaga(savedOrder);
        return savedOrder;
    }

    public Optional<Order> getOrder(UUID id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public Order cancelOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!"CANCELLED".equals(order.getStatus())) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            sagaOrchestrator.cancelSaga(order);
        }
        return order;
    }
}
