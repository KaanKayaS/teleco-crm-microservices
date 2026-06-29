package com.turkcell.customer_service.handler.query;

import com.turkcell.customer_service.cqrs.IQueryHandler;
import com.turkcell.customer_service.dto.response.CustomerResponse;
import com.turkcell.customer_service.entity.Customer;
import com.turkcell.customer_service.exception.CustomerNotFoundException;
import com.turkcell.customer_service.handler.CustomerMapper;
import com.turkcell.customer_service.query.GetCustomerQuery;
import com.turkcell.customer_service.repository.CustomerRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ID'ye göre müşteri bilgilerini döner.
 */
@Component
public class GetCustomerQueryHandler
        implements IQueryHandler<GetCustomerQuery, CustomerResponse> {

    private final CustomerRepository customerRepository;

    public GetCustomerQueryHandler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse handle(GetCustomerQuery query) {
        Customer customer = customerRepository.findById(query.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(query.customerId()));

        return CustomerMapper.toResponse(customer);
    }
}
