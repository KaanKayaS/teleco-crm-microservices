package com.turkcell.customer_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(

        @NotBlank(message = "Ad boş olamaz")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Soyad boş olamaz")
        @Size(max = 100)
        String lastName,

        @NotBlank(message = "TC Kimlik numarası boş olamaz")
        @Size(min = 11, max = 20, message = "TC Kimlik 11 karakter olmalıdır")
        String identityNumber,

        @Pattern(regexp = "^[+\\d\\s\\-()]{7,20}$", message = "Geçersiz telefon numarası formatı")
        String phone,

        @Email(message = "Geçersiz e-posta adresi")
        @Size(max = 150)
        String email
) {}
