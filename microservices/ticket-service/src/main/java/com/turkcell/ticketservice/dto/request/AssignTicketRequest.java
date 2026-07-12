package com.turkcell.ticketservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Ticket atama isteği")
public class AssignTicketRequest {

    @NotNull(message = "Atanacak kişi ID boş olamaz")
    @Schema(description = "Ticket'in atanacağı agent/staff UUID", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID assignedTo;
}
