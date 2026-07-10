package com.turkcell.billingservice.client;

import java.math.BigDecimal;

public class TariffDto {
    private String code;
    private BigDecimal monthlyFee;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(BigDecimal monthlyFee) {
        this.monthlyFee = monthlyFee;
    }
}
