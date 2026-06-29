package com.turkcell.customer_service.query;

import com.turkcell.customer_service.cqrs.IQuery;
import com.turkcell.customer_service.dto.response.CustomerResponse;

import java.util.UUID;

/**
 * Query: ID'ye göre müşteri getir.
 */
public record GetCustomerQuery(UUID customerId) implements IQuery<CustomerResponse> {}
