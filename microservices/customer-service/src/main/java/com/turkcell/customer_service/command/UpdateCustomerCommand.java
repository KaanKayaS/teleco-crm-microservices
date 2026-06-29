package com.turkcell.customer_service.command;

import com.turkcell.customer_service.cqrs.ICommand;
import com.turkcell.customer_service.dto.response.CustomerResponse;

import java.util.UUID;

/**
 * Command: mevcut müşteriyi güncelle.
 */
public record UpdateCustomerCommand(
        UUID customerId,
        String firstName,
        String lastName,
        String phone,
        String email
) implements ICommand<CustomerResponse> {}
