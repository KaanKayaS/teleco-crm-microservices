package com.turkcell.subscriptionservice.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "msisdn", nullable = false, length = 20)
    private String msisdn;

    @Column(name = "tariff_code", nullable = false, length = 50)
    private String tariffCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "activated_at")
    private OffsetDateTime activatedAt;

    @Column(name = "terminated_at")
    private OffsetDateTime terminatedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = SubscriptionStatus.ACTIVE;
        }
        activatedAt = OffsetDateTime.now();
    }

    public Subscription() {}

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
