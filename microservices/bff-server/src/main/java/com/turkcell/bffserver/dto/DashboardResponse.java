package com.turkcell.bffserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Aggregated dashboard response — merges 4 microservice responses into one.
 * Angular makes ONE request to /bff/dashboard instead of 4 separate calls.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {

    private Map<String, Object> customer;
    private Map<String, Object> subscription;
    private Map<String, Object> usage;
    private List<Map<String, Object>> recentInvoices;

    public DashboardResponse() {}

    public DashboardResponse(Map<String, Object> customer,
                              Map<String, Object> subscription,
                              Map<String, Object> usage,
                              List<Map<String, Object>> recentInvoices) {
        this.customer = customer;
        this.subscription = subscription;
        this.usage = usage;
        this.recentInvoices = recentInvoices;
    }

    public Map<String, Object> getCustomer() { return customer; }
    public void setCustomer(Map<String, Object> customer) { this.customer = customer; }

    public Map<String, Object> getSubscription() { return subscription; }
    public void setSubscription(Map<String, Object> subscription) { this.subscription = subscription; }

    public Map<String, Object> getUsage() { return usage; }
    public void setUsage(Map<String, Object> usage) { this.usage = usage; }

    public List<Map<String, Object>> getRecentInvoices() { return recentInvoices; }
    public void setRecentInvoices(List<Map<String, Object>> recentInvoices) { this.recentInvoices = recentInvoices; }
}
