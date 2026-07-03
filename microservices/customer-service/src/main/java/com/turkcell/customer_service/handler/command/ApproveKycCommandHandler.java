package com.turkcell.customer_service.handler.command;

import com.turkcell.customer_service.command.ApproveKycCommand;
import com.turkcell.customer_service.cqrs.ICommandHandler;
import com.turkcell.customer_service.dto.response.CustomerResponse;
import com.turkcell.customer_service.entity.Customer;
import com.turkcell.customer_service.exception.CustomerNotFoundException;
import com.turkcell.customer_service.handler.CustomerMapper;
import com.turkcell.customer_service.repository.CustomerRepository;
import com.turkcell.customer_service.entity.OutboxEvent;
import com.turkcell.customer_service.entity.event.CustomerKYCApprovedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Müşterinin KYC onay durumunu {@code KYC_APPROVED} olarak işaretler.
 */
@Component
public class ApproveKycCommandHandler
        implements ICommandHandler<ApproveKycCommand, CustomerResponse> {

    private final CustomerRepository customerRepository;
    private final com.turkcell.customer_service.repository.OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ApproveKycCommandHandler(CustomerRepository customerRepository,
                                    com.turkcell.customer_service.repository.OutboxEventRepository outboxEventRepository,
                                    ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
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

        // Outbox event kaydı
        CustomerKYCApprovedEvent eventPayload = new CustomerKYCApprovedEvent(
                customer.getId(),
                customer.getIdentityNumber(),
                OffsetDateTime.now()
        );

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("Customer");
        outboxEvent.setAggregateId(customer.getId().toString());
        outboxEvent.setType("CustomerKYCApproved");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = objectMapper.convertValue(eventPayload, Map.class);
        outboxEvent.setPayload(payloadMap);
        
        outboxEventRepository.save(outboxEvent);

        return CustomerMapper.toResponse(customer);
    }
}
