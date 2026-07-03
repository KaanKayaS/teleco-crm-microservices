package com.turkcell.customer_service.handler.command;

import com.turkcell.customer_service.command.CreateCustomerCommand;
import com.turkcell.customer_service.cqrs.ICommandHandler;
import com.turkcell.customer_service.dto.response.CustomerResponse;
import com.turkcell.customer_service.entity.Customer;
import com.turkcell.customer_service.handler.CustomerMapper;
import com.turkcell.customer_service.repository.CustomerRepository;
import com.turkcell.customer_service.entity.OutboxEvent;
import com.turkcell.customer_service.entity.event.CustomerRegisteredEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Yeni müşteri kaydı oluşturur.
 * <p>TC Kimlik numarasının tekrar eden kayıt olup olmadığını kontrol eder.</p>
 */
@Component
public class CreateCustomerCommandHandler
        implements ICommandHandler<CreateCustomerCommand, CustomerResponse> {

    private final CustomerRepository customerRepository;
    private final com.turkcell.customer_service.repository.OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public CreateCustomerCommandHandler(CustomerRepository customerRepository, 
                                        com.turkcell.customer_service.repository.OutboxEventRepository outboxEventRepository,
                                        ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
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

        // Outbox event kaydı
        CustomerRegisteredEvent eventPayload = new CustomerRegisteredEvent(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getIdentityNumber(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCreatedAt()
        );

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("Customer");
        outboxEvent.setAggregateId(customer.getId().toString());
        outboxEvent.setType("CustomerRegistered");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = objectMapper.convertValue(eventPayload, Map.class);
        outboxEvent.setPayload(payloadMap);
        
        outboxEventRepository.save(outboxEvent);

        return CustomerMapper.toResponse(customer);
    }
}
