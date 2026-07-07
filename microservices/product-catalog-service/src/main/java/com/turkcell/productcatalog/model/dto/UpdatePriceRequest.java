package com.turkcell.productcatalog.model.dto;

import java.math.BigDecimal;

public class UpdatePriceRequest {
    private BigDecimal price;

    public UpdatePriceRequest() {
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
