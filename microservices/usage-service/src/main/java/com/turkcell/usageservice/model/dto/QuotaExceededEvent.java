package com.turkcell.usageservice.model.dto;

import java.util.UUID;

public class QuotaExceededEvent {
    private UUID subscriptionId;
    private String type; // VOICE, SMS, DATA
    private String message;

    public QuotaExceededEvent() {}

    public QuotaExceededEvent(UUID subscriptionId, String type, String message) {
        this.subscriptionId = subscriptionId;
        this.type = type;
        this.message = message;
    }

    public UUID getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(UUID subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
