package com.turkcell.customer_service.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(UUID customerId) {
        super("Müşteri bulunamadı: " + customerId);
    }
}
