package com.turkcell.customer_service.controller;

import com.turkcell.customer_service.dto.CustomerRegistrationDTO;
import com.turkcell.customer_service.entity.Customer;
import com.turkcell.customer_service.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody CustomerRegistrationDTO dto) {
        Customer customer = customerService.createCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable UUID id) {
        Customer customer = customerService.getCustomer(id);
        return ResponseEntity.ok(customer);
    }
}
