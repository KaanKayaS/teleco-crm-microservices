package com.turkcell.paymentservice.dto;

import com.turkcell.paymentservice.model.entity.PaymentMethod;
import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class PaymentRequest {
    @NotBlank
    private String invoiceId;

    private String customerId;

    @NotNull
    private BigDecimal amount;
    private PaymentMethod method;
    private String paymentRequestId;
}
