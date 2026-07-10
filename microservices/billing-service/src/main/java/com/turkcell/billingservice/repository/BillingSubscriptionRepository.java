package com.turkcell.billingservice.repository;

import com.turkcell.billingservice.model.entity.BillingSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BillingSubscriptionRepository extends JpaRepository<BillingSubscription, UUID> {
    List<BillingSubscription> findByCustomerIdAndStatus(UUID customerId, String status);
}
