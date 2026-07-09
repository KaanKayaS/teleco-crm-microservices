package com.turkcell.subscriptionservice.repository;

import com.turkcell.subscriptionservice.model.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByCustomerId(UUID customerId);
}
