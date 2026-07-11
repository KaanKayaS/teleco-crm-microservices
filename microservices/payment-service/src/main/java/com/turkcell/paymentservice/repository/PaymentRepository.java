package com.turkcell.paymentservice.repository;

import com.turkcell.paymentservice.model.entity.Payment;
import com.turkcell.paymentservice.model.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByPaymentRequestId(String paymentRequestId);
    List<Payment> findByStatus(PaymentStatus status);
}
