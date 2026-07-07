package com.turkcell.orderservice.repository;

import com.turkcell.orderservice.model.entity.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SagaStateRepository extends JpaRepository<SagaState, UUID> {
    Optional<SagaState> findByOrderId(UUID orderId);
}
