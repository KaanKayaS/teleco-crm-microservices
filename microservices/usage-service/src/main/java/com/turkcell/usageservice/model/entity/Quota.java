package com.turkcell.usageservice.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quotas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"subscription_id", "period_start", "period_end"})
})
public class Quota {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "minutes_remaining", nullable = false)
    private Integer minutesRemaining;

    @Column(name = "sms_remaining", nullable = false)
    private Integer smsRemaining;

    @Column(name = "mb_remaining", nullable = false)
    private Integer mbRemaining;

    @Version
    private Long version;

    public Quota() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Integer getMinutesRemaining() {
        return minutesRemaining;
    }

    public void setMinutesRemaining(Integer minutesRemaining) {
        this.minutesRemaining = minutesRemaining;
    }

    public Integer getSmsRemaining() {
        return smsRemaining;
    }

    public void setSmsRemaining(Integer smsRemaining) {
        this.smsRemaining = smsRemaining;
    }

    public Integer getMbRemaining() {
        return mbRemaining;
    }

    public void setMbRemaining(Integer mbRemaining) {
        this.mbRemaining = mbRemaining;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
