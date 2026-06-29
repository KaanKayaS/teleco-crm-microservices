package com.turkcell.customer_service.handler.command;

import com.turkcell.customer_service.command.ApproveKycCommand;
import com.turkcell.customer_service.cqrs.ICommandHandler;
import com.turkcell.customer_service.dto.response.CustomerResponse;
import com.turkcell.customer_service.entity.Customer;
import com.turkcell.customer_service.exception.CustomerNotFoundException;
import com.turkcell.customer_service.handler.CustomerMapper;
import com.turkcell.customer_service.repository.CustomerRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Müşterinin KYC onay durumunu {@code KYC_APPROVED} olarak işaretler.
 */
@Component
public class ApproveKycCommandHandler
        implements ICommandHandler<ApproveKycCommand, CustomerResponse> {

    private final CustomerRepository customerRepository;

    public ApproveKycCommandHandler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerResponse handle(ApproveKycCommand command) {
        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));

        if (customer.isApproved()) {
            throw new IllegalArgumentException("Müşteri zaten KYC onaylı: " + command.customerId());
        }

        customer.setApproved(true);
        customer.setStatus("KYC_APPROVED");

        customer = customerRepository.save(customer);
        return CustomerMapper.toResponse(customer);
    }
}
