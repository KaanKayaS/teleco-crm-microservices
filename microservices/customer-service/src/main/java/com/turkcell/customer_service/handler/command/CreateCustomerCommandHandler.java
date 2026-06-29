package com.turkcell.customer_service.handler.command;

import com.turkcell.customer_service.command.CreateCustomerCommand;
import com.turkcell.customer_service.cqrs.ICommandHandler;
import com.turkcell.customer_service.dto.response.CustomerResponse;
import com.turkcell.customer_service.entity.Customer;
import com.turkcell.customer_service.handler.CustomerMapper;
import com.turkcell.customer_service.repository.CustomerRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Yeni müşteri kaydı oluşturur.
 * <p>TC Kimlik numarasının tekrar eden kayıt olup olmadığını kontrol eder.</p>
 */
@Component
public class CreateCustomerCommandHandler
        implements ICommandHandler<CreateCustomerCommand, CustomerResponse> {

    private final CustomerRepository customerRepository;

    public CreateCustomerCommandHandler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerResponse handle(CreateCustomerCommand command) {
        if (customerRepository.existsByIdentityNumber(command.identityNumber())) {
            throw new IllegalArgumentException(
                    "Bu TC Kimlik numarasına ait müşteri zaten mevcut: " + command.identityNumber());
        }

        Customer customer = new Customer();
        customer.setUserId(command.userId());
        customer.setFirstName(command.firstName());
        customer.setLastName(command.lastName());
        customer.setIdentityNumber(command.identityNumber());
        customer.setPhone(command.phone());
        customer.setEmail(command.email());
        customer.setStatus("ACTIVE");

        customer = customerRepository.save(customer);
        return CustomerMapper.toResponse(customer);
    }
}
