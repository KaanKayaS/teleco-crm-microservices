package com.turkcell.customer_service.command;

import com.turkcell.customer_service.cqrs.ICommand;
import com.turkcell.customer_service.dto.response.CustomerResponse;

/**
 * Command: yeni müşteri oluştur.
 */
public record CreateCustomerCommand(
        String userId,
        String firstName,
        String lastName,
        String identityNumber,
        String phone,
        String email
) implements ICommand<CustomerResponse> {}
