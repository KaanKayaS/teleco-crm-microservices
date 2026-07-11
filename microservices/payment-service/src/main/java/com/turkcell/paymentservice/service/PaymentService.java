package com.turkcell.paymentservice.service;

import com.turkcell.paymentservice.dto.PaymentRequest;
import com.turkcell.paymentservice.dto.PaymentResponse;
import com.turkcell.paymentservice.messaging.PaymentKafkaProducer;
import com.turkcell.paymentservice.model.entity.Payment;
import com.turkcell.paymentservice.model.entity.PaymentAttempt;
import com.turkcell.paymentservice.model.entity.PaymentStatus;
import com.turkcell.paymentservice.repository.PaymentAttemptRepository;
import com.turkcell.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentKafkaProducer kafkaProducer;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for invoice: {}", request.getInvoiceId());

        // Idempotency Check
        Optional<Payment> existingPayment = paymentRepository.findByPaymentRequestId(request.getPaymentRequestId());
        if (existingPayment.isPresent()) {
            log.info("Payment with request id {} already processed", request.getPaymentRequestId());
            return mapToResponse(existingPayment.get());
        }

        Payment payment = Payment.builder()
                .invoiceId(request.getInvoiceId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .paymentRequestId(request.getPaymentRequestId())
                .build();

        payment = paymentRepository.save(payment);

        executePaymentAttempt(payment);

        return mapToResponse(payment);
    }

    private void executePaymentAttempt(Payment payment) {
        // Mock PSP call
        boolean isSuccess = Math.random() > 0.2; // 80% success rate

        PaymentAttempt attempt = PaymentAttempt.builder()
                .paymentId(payment.getId())
                .attemptNo(1)
                .attemptedAt(LocalDateTime.now())
                .response(isSuccess ? "PSP_SUCCESS" : "PSP_DECLINED")
                .build();
        paymentAttemptRepository.save(attempt);

        if (isSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setExternalRef(UUID.randomUUID().toString());
            payment.setPaidAt(LocalDateTime.now());
            kafkaProducer.publishPaymentCompletedEvent(payment.getId(), payment.getInvoiceId(), payment.getCustomerId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            kafkaProducer.publishPaymentFailedEvent(payment.getId(), payment.getInvoiceId(), payment.getCustomerId(), "PSP_DECLINED");
        }

        paymentRepository.save(payment);
    }

    public PaymentResponse getPayment(String id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(String id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            kafkaProducer.publishPaymentRefundedEvent(payment.getId(), payment.getInvoiceId(), payment.getCustomerId());
        }
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        BeanUtils.copyProperties(payment, response);
        return response;
    }

    // Cron job to run every hour to find failed payments and retry them 
    // simulating 24, 72, 168 hours delays
    @Scheduled(cron = "0 0 * * * *")
    public void retryFailedPayments() {
        log.info("Running scheduled job to retry failed payments...");
        List<Payment> failedPayments = paymentRepository.findByStatus(PaymentStatus.FAILED);
        for (Payment payment : failedPayments) {
            // Simplified logic: retry if older than 24 hours (simulating the 24/72/168 flow)
            if (payment.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
                executePaymentAttempt(payment);
            }
        }
    }
}
