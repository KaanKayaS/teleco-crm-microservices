package com.turkcell.customer_service.handler.command;

import com.turkcell.customer_service.command.UpdateCustomerCommand;
import com.turkcell.customer_service.cqrs.ICommandHandler;
import com.turkcell.customer_service.dto.response.CustomerResponse;
import com.turkcell.customer_service.entity.Customer;
import com.turkcell.customer_service.exception.CustomerNotFoundException;
import com.turkcell.customer_service.handler.CustomerMapper;
import com.turkcell.customer_service.repository.CustomerRepository;
import com.turkcell.customer_service.entity.OutboxEvent;
import com.turkcell.customer_service.entity.event.CustomerUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Mevcut müşterinin iletişim bilgilerini günceller.
 */
@Component
public class UpdateCustomerCommandHandler
        implements ICommandHandler<UpdateCustomerCommand, CustomerResponse> {

    private final CustomerRepository customerRepository;
    private final com.turkcell.customer_service.repository.OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public UpdateCustomerCommandHandler(CustomerRepository customerRepository,
                                        com.turkcell.customer_service.repository.OutboxEventRepository outboxEventRepository,
                                        ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
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

        // Outbox event kaydı
        CustomerUpdatedEvent eventPayload = new CustomerUpdatedEvent(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                OffsetDateTime.now()
        );

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("Customer");
        outboxEvent.setAggregateId(customer.getId().toString());
        outboxEvent.setType("CustomerUpdated");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = objectMapper.convertValue(eventPayload, Map.class);
        outboxEvent.setPayload(payloadMap);
        
        outboxEventRepository.save(outboxEvent);

        return CustomerMapper.toResponse(customer);
    }
}
