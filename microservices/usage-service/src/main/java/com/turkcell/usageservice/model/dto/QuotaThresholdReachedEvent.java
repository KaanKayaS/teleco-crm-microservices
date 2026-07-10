package com.turkcell.usageservice.model.dto;

import java.util.UUID;

public class QuotaThresholdReachedEvent {
    private UUID subscriptionId;
    private String type; // VOICE, SMS, DATA
    private Integer remaining;
    private String message;

    public QuotaThresholdReachedEvent() {}

    public QuotaThresholdReachedEvent(UUID subscriptionId, String type, Integer remaining, String message) {
        this.subscriptionId = subscriptionId;
        this.type = type;
        this.remaining = remaining;
        this.message = message;
    }

    public UUID getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(UUID subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getRemaining() { return remaining; }
    public void setRemaining(Integer remaining) { this.remaining = remaining; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
