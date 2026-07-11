package com.turkcell.paymentservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompletedEvent(String paymentId, String invoiceId) {
        log.info("Publishing PaymentCompletedEvent for invoice: {}", invoiceId);
        kafkaTemplate.send("payment-completed-events", invoiceId, Map.of(
                "paymentId", paymentId,
                "invoiceId", invoiceId,
                "status", "SUCCESS"
        ));
    }

    public void publishPaymentFailedEvent(String paymentId, String invoiceId, String reason) {
        log.info("Publishing PaymentFailedEvent for invoice: {}", invoiceId);
        kafkaTemplate.send("payment-failed-events", invoiceId, Map.of(
                "paymentId", paymentId,
                "invoiceId", invoiceId,
                "reason", reason,
                "status", "FAILED"
        ));
    }

    public void publishPaymentRefundedEvent(String paymentId, String invoiceId) {
        log.info("Publishing PaymentRefundedEvent for invoice: {}", invoiceId);
        kafkaTemplate.send("payment-refunded-events", invoiceId, Map.of(
                "paymentId", paymentId,
                "invoiceId", invoiceId,
                "status", "REFUNDED"
        ));
    }
}
