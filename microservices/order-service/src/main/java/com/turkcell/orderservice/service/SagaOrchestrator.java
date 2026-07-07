package com.turkcell.orderservice.service;

import com.turkcell.orderservice.messaging.producer.OrderEventProducer;
import com.turkcell.orderservice.model.entity.Order;
import com.turkcell.orderservice.model.entity.SagaState;
import com.turkcell.orderservice.repository.SagaStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SagaOrchestrator {

    private final SagaStateRepository sagaStateRepository;
    private final OrderEventProducer eventProducer;

    public SagaOrchestrator(SagaStateRepository sagaStateRepository, OrderEventProducer eventProducer) {
        this.sagaStateRepository = sagaStateRepository;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public void startSaga(Order order) {
        SagaState state = new SagaState();
        state.setOrderId(order.getId());
        state.setCurrentStep("ORDER_CREATED");
        sagaStateRepository.save(state);
        
        eventProducer.publishOrderCreated(order);
    }

    @Transactional
    public void cancelSaga(Order order) {
        SagaState state = sagaStateRepository.findByOrderId(order.getId())
                .orElse(new SagaState());
        state.setOrderId(order.getId());
        state.setCurrentStep("ORDER_CANCELLED");
        sagaStateRepository.save(state);
        
        eventProducer.publishOrderCancelled(order);
    }
}
