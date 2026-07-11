package com.turkcell.paymentservice.controller;

import com.turkcell.paymentservice.dto.PaymentRequest;
import com.turkcell.paymentservice.dto.PaymentResponse;
import com.turkcell.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "Operations related to Payments")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Process a new payment")
    public PaymentResponse processPayment(@RequestBody PaymentRequest request) {
        return paymentService.processPayment(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment details by ID")
    public PaymentResponse getPayment(@PathVariable String id) {
        return paymentService.getPayment(id);
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund a processed payment")
    public PaymentResponse refundPayment(@PathVariable String id) {
        return paymentService.refundPayment(id);
    }
}
