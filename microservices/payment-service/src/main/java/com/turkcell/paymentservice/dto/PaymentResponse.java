package com.turkcell.paymentservice.dto;

import com.turkcell.paymentservice.model.entity.PaymentMethod;
import com.turkcell.paymentservice.model.entity.PaymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private String id;
    private String invoiceId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String externalRef;
    private LocalDateTime paidAt;
}
