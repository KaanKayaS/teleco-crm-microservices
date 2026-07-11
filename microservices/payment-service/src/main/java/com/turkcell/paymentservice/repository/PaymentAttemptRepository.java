package com.turkcell.paymentservice.repository;

import com.turkcell.paymentservice.model.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, String> {
}
