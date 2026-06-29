package com.turkcell.customer_service.handler.command;

import com.turkcell.customer_service.command.UpdateCustomerCommand;
import com.turkcell.customer_service.cqrs.ICommandHandler;
import com.turkcell.customer_service.dto.response.CustomerResponse;
import com.turkcell.customer_service.entity.Customer;
import com.turkcell.customer_service.exception.CustomerNotFoundException;
import com.turkcell.customer_service.handler.CustomerMapper;
import com.turkcell.customer_service.repository.CustomerRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mevcut müşterinin iletişim bilgilerini günceller.
 */
@Component
public class UpdateCustomerCommandHandler
        implements ICommandHandler<UpdateCustomerCommand, CustomerResponse> {

    private final CustomerRepository customerRepository;

    public UpdateCustomerCommandHandler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerResponse handle(UpdateCustomerCommand command) {
        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));

        customer.setFirstName(command.firstName());
        customer.setLastName(command.lastName());
        customer.setPhone(command.phone());
        customer.setEmail(command.email());

        customer = customerRepository.save(customer);
        return CustomerMapper.toResponse(customer);
    }
}
