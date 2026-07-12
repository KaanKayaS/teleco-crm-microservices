package com.turkcell.ticketservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Ticket yorum yanıtı")
public class TicketCommentResponse {

    @Schema(description = "Yorum UUID")
    private UUID id;

    @Schema(description = "Ticket UUID")
    private UUID ticketId;

    @Schema(description = "Yazar UUID")
    private UUID authorId;

    @Schema(description = "Yorum içeriği")
    private String body;

    @Schema(description = "Oluşturulma zamanı")
    private ZonedDateTime createdAt;
}
