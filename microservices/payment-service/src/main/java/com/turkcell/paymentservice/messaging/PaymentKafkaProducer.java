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

    public void publishPaymentCompletedEvent(String paymentId, String invoiceId, String customerId) {
        log.info("Publishing PaymentCompletedEvent for invoice: {}", invoiceId);
        kafkaTemplate.send("payment-events", invoiceId, Map.of(
                "eventType", "PaymentCompleted",
                "paymentId", paymentId,
                "invoiceId", invoiceId,
                "customerId", customerId != null ? customerId : "",
                "status", "SUCCESS"
        ));
    }

    public void publishPaymentFailedEvent(String paymentId, String invoiceId, String customerId, String reason) {
        log.info("Publishing PaymentFailedEvent for invoice: {}", invoiceId);
        kafkaTemplate.send("payment-events", invoiceId, Map.of(
                "eventType", "PaymentFailed",
                "paymentId", paymentId,
                "invoiceId", invoiceId,
                "customerId", customerId != null ? customerId : "",
                "reason", reason,
                "status", "FAILED"
        ));
    }

    public void publishPaymentRefundedEvent(String paymentId, String invoiceId, String customerId) {
        log.info("Publishing PaymentRefundedEvent for invoice: {}", invoiceId);
        kafkaTemplate.send("payment-events", invoiceId, Map.of(
                "eventType", "PaymentRefunded",
                "paymentId", paymentId,
                "invoiceId", invoiceId,
                "customerId", customerId != null ? customerId : "",
                "status", "REFUNDED"
        ));
    }
}
