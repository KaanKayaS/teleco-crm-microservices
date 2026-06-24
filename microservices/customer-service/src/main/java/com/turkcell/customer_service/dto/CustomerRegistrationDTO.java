package com.turkcell.customer_service.dto;

public record CustomerRegistrationDTO(
        String userId,
        String firstName,
        String lastName,
        String identityNumber
) {}
