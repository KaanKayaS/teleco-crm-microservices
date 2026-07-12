package com.turkcell.ticketservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Ticket'e yorum ekleme isteği")
public class AddCommentRequest {

    @NotNull(message = "Yazar ID boş olamaz")
    @Schema(description = "Yorumu ekleyen kullanıcı/agent UUID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID authorId;

    @NotBlank(message = "Yorum içeriği boş olamaz")
    @Schema(description = "Yorum metni", example = "Sorun incelemeye alındı, teknik ekibimiz yönlendirildi.")
    private String body;
}
