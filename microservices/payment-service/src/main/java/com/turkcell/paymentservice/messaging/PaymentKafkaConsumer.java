package com.turkcell.paymentservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaConsumer {

    @KafkaListener(topics = "invoice-generated-events", groupId = "payment-service-group")
    public void consumeInvoiceGeneratedEvent(Map<String, Object> event) {
        log.info("Consumed InvoiceGeneratedEvent: {}", event);
        // Here we would trigger auto-pay logic if customer has a valid wallet or saved card
        // String invoiceId = (String) event.get("invoiceId");
        // BigDecimal amount = new BigDecimal(event.get("amount").toString());
        // String customerId = (String) event.get("customerId");
        // paymentService.processAutoPayment(invoiceId, customerId, amount);
    }
}
