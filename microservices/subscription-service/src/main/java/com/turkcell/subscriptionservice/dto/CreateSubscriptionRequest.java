package com.turkcell.subscriptionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for creating a new subscription.
 * Called internally by the Order Service after an OrderConfirmed event.
 */
public class CreateSubscriptionRequest {

    @NotNull(message = "customerId is required")
    private UUID customerId;

    @NotBlank(message = "tariffCode is required")
    private String tariffCode;

    // orderId is kept for traceability / saga correlation
    private UUID orderId;

    public CreateSubscriptionRequest() {}

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getTariffCode() {
        return tariffCode;
    }

    public void setTariffCode(String tariffCode) {
        this.tariffCode = tariffCode;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
