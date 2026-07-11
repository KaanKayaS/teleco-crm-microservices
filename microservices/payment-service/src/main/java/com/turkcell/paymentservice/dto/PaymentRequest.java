package com.turkcell.paymentservice.dto;

import com.turkcell.paymentservice.model.entity.PaymentMethod;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private String invoiceId;
    private BigDecimal amount;
    private PaymentMethod method;
    private String paymentRequestId;
}
