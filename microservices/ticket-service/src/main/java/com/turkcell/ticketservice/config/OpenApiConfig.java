package com.turkcell.ticketservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Ticket Service API",
        version = "1.0",
        description = """
            Müşteri Talep/Şikayet Yönetimi ve SLA Takip Servisi.
            
            **Özellikler:**
            - FR-31: Müşteriler şikayet, talep ve arıza kaydı açabilir
            - FR-32: Ticket priority'e göre otomatik SLA tarihi hesaplanır (CRITICAL=2s, HIGH=8s, MEDIUM=24s, LOW=72s)
            - FR-33: Ticket açıldığında/güncellendiğinde Kafka event yayımlanır
            
            **Events:** TicketOpened, TicketCommentAdded, TicketAssigned, TicketResolved, SlaBreached
            """,
        contact = @Contact(name = "Telco CRM Team")
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Keycloak'tan alınan JWT token'ı buraya girin. Format: Bearer <token>"
)
public class OpenApiConfig {
}
