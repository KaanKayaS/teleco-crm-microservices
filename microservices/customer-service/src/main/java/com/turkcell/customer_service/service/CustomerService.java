package com.turkcell.customer_service.service;

import com.turkcell.customer_service.dto.CustomerRegistrationDTO;
import com.turkcell.customer_service.entity.Customer;
import com.turkcell.customer_service.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCustomer(CustomerRegistrationDTO dto) {
        Customer customer = new Customer();
        customer.setUserId(dto.userId());
        customer.setFirstName(dto.firstName());
        customer.setLastName(dto.lastName());
        customer.setIdentityNumber(dto.identityNumber());
        customer.setStatus("ACTIVE");
        
        return customerRepository.save(customer);
    }

    public Customer getCustomer(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
    }
}
