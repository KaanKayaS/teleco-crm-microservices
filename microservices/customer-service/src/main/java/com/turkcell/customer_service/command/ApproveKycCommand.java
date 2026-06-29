package com.turkcell.customer_service.command;

import com.turkcell.customer_service.cqrs.ICommand;
import com.turkcell.customer_service.dto.response.CustomerResponse;

import java.util.UUID;

/**
 * Command: müşterinin KYC durumunu onayla.
 */
public record ApproveKycCommand(
        UUID customerId
) implements ICommand<CustomerResponse> {}
