package com.turkcell.ticketservice.dto.response;

import com.turkcell.ticketservice.model.entity.TicketCategory;
import com.turkcell.ticketservice.model.entity.TicketPriority;
import com.turkcell.ticketservice.model.entity.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Ticket detay yanıtı")
public class TicketResponse {

    @Schema(description = "Ticket UUID")
    private UUID id;

    @Schema(description = "Müşteri UUID")
    private UUID customerId;

    @Schema(description = "Ticket kategorisi")
    private TicketCategory category;

    @Schema(description = "Ticket önceliği")
    private TicketPriority priority;

    @Schema(description = "Ticket durumu")
    private TicketStatus status;

    @Schema(description = "Sorunun açıklaması")
    private String description;

    @Schema(description = "Atanan agent/staff UUID")
    private UUID assignedTo;

    @Schema(description = "SLA son tarihi")
    private ZonedDateTime slaDueAt;

    @Schema(description = "Oluşturulma zamanı")
    private ZonedDateTime createdAt;

    @Schema(description = "Çözülme zamanı")
    private ZonedDateTime resolvedAt;

    @Schema(description = "SLA ihlali yaşandı mı")
    private boolean slaBreached;

    @Schema(description = "Ticket yorumları")
    private List<TicketCommentResponse> comments;
}
