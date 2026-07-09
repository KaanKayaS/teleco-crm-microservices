package com.turkcell.subscriptionservice.dto;

import com.turkcell.subscriptionservice.model.entity.SubscriptionStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for subscription queries and state transitions.
 */
public class SubscriptionResponse {

    private UUID id;
    private UUID customerId;
    private String msisdn;
    private String tariffCode;
    private SubscriptionStatus status;
    private OffsetDateTime activatedAt;
    private OffsetDateTime terminatedAt;

    public SubscriptionResponse() {}

    // --- Getters & Setters ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getTariffCode() {
        return tariffCode;
    }

    public void setTariffCode(String tariffCode) {
        this.tariffCode = tariffCode;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public OffsetDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(OffsetDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public OffsetDateTime getTerminatedAt() {
        return terminatedAt;
    }

    public void setTerminatedAt(OffsetDateTime terminatedAt) {
        this.terminatedAt = terminatedAt;
    }
}
