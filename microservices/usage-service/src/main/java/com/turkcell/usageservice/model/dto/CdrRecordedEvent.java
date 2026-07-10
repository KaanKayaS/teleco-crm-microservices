package com.turkcell.usageservice.model.dto;

import java.time.LocalDateTime;

public class CdrRecordedEvent {
    private String cdrRef;
    private String subscriptionId;
    private String type; // VOICE, SMS, DATA
    private Integer quantity; // seconds, count, MB
    private String timestamp;

    public CdrRecordedEvent() {}

    public String getCdrRef() { return cdrRef; }
    public void setCdrRef(String cdrRef) { this.cdrRef = cdrRef; }

    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
