package com.turkcell.ticketservice.dto.request;

import com.turkcell.ticketservice.model.entity.TicketCategory;
import com.turkcell.ticketservice.model.entity.TicketPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yeni ticket oluşturma isteği")
public class CreateTicketRequest {

    @NotNull(message = "Müşteri ID boş olamaz")
    @Schema(description = "Müşteri UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID customerId;

    @NotNull(message = "Kategori boş olamaz")
    @Schema(description = "Ticket kategorisi: COMPLAINT, REQUEST, FAULT", example = "FAULT")
    private TicketCategory category;

    @NotNull(message = "Öncelik boş olamaz")
    @Schema(description = "Ticket önceliği: LOW, MEDIUM, HIGH, CRITICAL", example = "HIGH")
    private TicketPriority priority;

    @NotBlank(message = "Açıklama boş olamaz")
    @Schema(description = "Sorunun/talebin açıklaması", example = "İnternet bağlantım 2 saattir yok.")
    private String description;
}
