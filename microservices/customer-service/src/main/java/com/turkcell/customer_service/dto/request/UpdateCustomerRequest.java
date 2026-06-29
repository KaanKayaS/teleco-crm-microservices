package com.turkcell.customer_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(

        @NotBlank(message = "Ad boş olamaz")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Soyad boş olamaz")
        @Size(max = 100)
        String lastName,

        @Pattern(regexp = "^[+\\d\\s\\-()]{7,20}$", message = "Geçersiz telefon numarası formatı")
        String phone,

        @Email(message = "Geçersiz e-posta adresi")
        @Size(max = 150)
        String email
) {}
