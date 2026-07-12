package com.turkcell.ticketservice.controller;

import com.turkcell.ticketservice.dto.request.AddCommentRequest;
import com.turkcell.ticketservice.dto.request.AssignTicketRequest;
import com.turkcell.ticketservice.dto.request.CreateTicketRequest;
import com.turkcell.ticketservice.dto.response.TicketCommentResponse;
import com.turkcell.ticketservice.dto.response.TicketResponse;
import com.turkcell.ticketservice.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Ticket Yönetimi", description = "Müşteri talep/şikayet yönetimi ve SLA takip API'leri")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // ------------------------------------------------------------------
    // POST /api/v1/tickets
    // ------------------------------------------------------------------
    @Operation(
        summary = "Yeni Ticket Oluştur",
        description = "FR-31: Müşteriler şikayet, talep ve arıza kaydı açabilir. " +
                      "FR-32: Ticket, priority'e göre otomatik SLA tarihi ile oluşturulur. " +
                      "FR-33: Oluşturma anında TicketOpened eventi yayımlanır."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ticket başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek parametreleri"),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekiyor"),
        @ApiResponse(responseCode = "403", description = "Yetersiz yetki")
    })
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ------------------------------------------------------------------
    // GET /api/v1/tickets/{id}
    // ------------------------------------------------------------------
    @Operation(
        summary = "Ticket Detayı Getir",
        description = "Ticket ID'sine göre yorum listesi dahil ticket detaylarını döner."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket bulundu"),
        @ApiResponse(responseCode = "404", description = "Ticket bulunamadı"),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekiyor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(
            @Parameter(description = "Ticket UUID", required = true)
            @PathVariable UUID id) {
        return ticketService.getTicket(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // GET /api/v1/tickets?customerId=...
    // ------------------------------------------------------------------
    @Operation(
        summary = "Müşteriye Ait Ticket'ları Listele",
        description = "Müşteri ID'sine göre tüm ticket'ları oluşturulma tarihine göre (en yeni önce) listeler."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket listesi döndürüldü"),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekiyor")
    })
    @GetMapping
    public ResponseEntity<List<TicketResponse>> listByCustomer(
            @Parameter(description = "Müşteri UUID", required = true)
            @RequestParam UUID customerId) {
        return ResponseEntity.ok(ticketService.listByCustomer(customerId));
    }

    // ------------------------------------------------------------------
    // POST /api/v1/tickets/{id}/comments
    // ------------------------------------------------------------------
    @Operation(
        summary = "Ticket'e Yorum Ekle",
        description = "Ticket'e yorum/not ekler. İlk yorum eklendiğinde ticket durumu IN_PROGRESS olur. " +
                      "TicketCommentAdded eventi yayımlanır."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Yorum eklendi"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek"),
        @ApiResponse(responseCode = "404", description = "Ticket bulunamadı"),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekiyor")
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<TicketCommentResponse> addComment(
            @Parameter(description = "Ticket UUID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody AddCommentRequest request) {
        TicketCommentResponse response = ticketService.addComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ------------------------------------------------------------------
    // POST /api/v1/tickets/{id}/assign
    // ------------------------------------------------------------------
    @Operation(
        summary = "Ticket Ata",
        description = "Ticket'i belirli bir agent/staff üyesine atar. " +
                      "FR-32: SLA bazlı atama akışının parçasıdır. TicketAssigned eventi yayımlanır."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket atandı"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek"),
        @ApiResponse(responseCode = "404", description = "Ticket bulunamadı"),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekiyor"),
        @ApiResponse(responseCode = "403", description = "Sadece BACKOFFICE_STAFF atayabilir")
    })
    @PostMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @Parameter(description = "Ticket UUID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody AssignTicketRequest request) {
        return ResponseEntity.ok(ticketService.assignTicket(id, request));
    }

    // ------------------------------------------------------------------
    // POST /api/v1/tickets/{id}/resolve
    // ------------------------------------------------------------------
    @Operation(
        summary = "Ticket'i Çöz",
        description = "Ticket durumunu RESOLVED olarak işaretler, çözülme zamanını kaydeder. " +
                      "TicketResolved eventi yayımlanır."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket çözüldü"),
        @ApiResponse(responseCode = "404", description = "Ticket bulunamadı"),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekiyor"),
        @ApiResponse(responseCode = "403", description = "Yetki gerekiyor")
    })
    @PostMapping("/{id}/resolve")
    public ResponseEntity<TicketResponse> resolveTicket(
            @Parameter(description = "Ticket UUID", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.resolveTicket(id));
    }
}
