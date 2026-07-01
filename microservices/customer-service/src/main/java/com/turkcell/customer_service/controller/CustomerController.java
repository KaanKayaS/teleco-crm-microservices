package com.turkcell.customer_service.controller;

import com.turkcell.customer_service.command.ApproveKycCommand;
import com.turkcell.customer_service.command.CreateCustomerCommand;
import com.turkcell.customer_service.command.UpdateCustomerCommand;
import com.turkcell.customer_service.command.UploadDocumentCommand;
import com.turkcell.customer_service.cqrs.Mediator;
import com.turkcell.customer_service.dto.request.CreateCustomerRequest;
import com.turkcell.customer_service.dto.request.UpdateCustomerRequest;
import com.turkcell.customer_service.dto.response.CustomerResponse;
import com.turkcell.customer_service.dto.response.DocumentResponse;
import com.turkcell.customer_service.query.GetCustomerQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer", description = "Müşteri kimlik ve iletişim bilgileri yönetimi (CQRS/Mediator)")
public class CustomerController {

    private final Mediator mediator;

    public CustomerController(Mediator mediator) {
        this.mediator = mediator;
    }

    // ----------------------------------------------------------------
    // POST /api/v1/customers
    // ----------------------------------------------------------------
    @Operation(
            summary = "Yeni müşteri oluştur",
            description = "Oturum açmış kullanıcının Keycloak kimliği (`X-User-Id` header) otomatik alınır. " +
                    "İstek gövdesinde sadece kişisel bilgiler gönderilir."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Müşteri başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Validasyon hatası veya mükerrer TC Kimlik", content = @Content),
            @ApiResponse(responseCode = "401", description = "X-User-Id header eksik — kimlik doğrulanmamış istek", content = @Content),
    })
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Parameter(description = "Keycloak tarafından eklenen kullanıcı ID'si (JWT sub claim) - Leave empty if using Bearer token", required = false)
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody CreateCustomerRequest request) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Kimlik doğrulaması gerekli: X-User-Id header bulunamadı");
        }

        CreateCustomerCommand command = new CreateCustomerCommand(
                userId,                  // ← JWT'den, body'den değil
                request.firstName(),
                request.lastName(),
                request.identityNumber(),
                request.phone(),
                request.email()
        );

        CustomerResponse response = mediator.send(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ----------------------------------------------------------------
    // GET /api/v1/customers/{id}
    // ----------------------------------------------------------------
    @Operation(
            summary = "Müşteri getir",
            description = "UUID ile müşteri bilgilerini ve KYC belgelerini döner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Müşteri bulundu"),
            @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı", content = @Content),
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @Parameter(description = "Müşteri UUID'si", required = true)
            @PathVariable UUID id) {

        CustomerResponse response = mediator.send(new GetCustomerQuery(id));
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // PUT /api/v1/customers/{id}
    // ----------------------------------------------------------------
    @Operation(
            summary = "Müşteri güncelle",
            description = "Ad, soyad, telefon ve e-posta bilgilerini günceller."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Güncelleme başarılı"),
            @ApiResponse(responseCode = "400", description = "Validasyon hatası", content = @Content),
            @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı", content = @Content),
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @Parameter(description = "Müşteri UUID'si", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {

        UpdateCustomerCommand command = new UpdateCustomerCommand(
                id,
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.email()
        );

        CustomerResponse response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // POST /api/v1/customers/{id}/documents  (multipart/form-data)
    // ----------------------------------------------------------------
    @Operation(
            summary = "KYC belgesi yükle",
            description = "Müşteriye ait kimlik belgesini (PDF, PNG, JPG) multipart/form-data olarak yükler. " +
                    "Dosya sunucuya `uploads/documents/{customerId}/` altına kaydedilir."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Belge başarıyla yüklendi"),
            @ApiResponse(responseCode = "400", description = "Boş dosya veya geçersiz istek", content = @Content),
            @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı", content = @Content),
    })
    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @Parameter(description = "Müşteri UUID'si", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Belge türü (ör: NUFUS_CUZDANI, PASAPORT, SURUCU_BELGESI)", required = true)
            @RequestParam("type") String type,
            @Parameter(description = "Yüklenecek belge dosyası (max 10 MB)", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Yüklenen dosya boş olamaz");
        }

        UploadDocumentCommand command = new UploadDocumentCommand(
                id,
                type,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );

        DocumentResponse response = mediator.send(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ----------------------------------------------------------------
    // POST /api/v1/customers/{id}/kyc/approve
    // ----------------------------------------------------------------
    @Operation(
            summary = "KYC onayla",
            description = "Müşterinin KYC durumunu `KYC_APPROVED` olarak işaretler. Zaten onaylı müşterilerde 400 döner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "KYC onayı başarılı"),
            @ApiResponse(responseCode = "400", description = "Müşteri zaten KYC onaylı", content = @Content),
            @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı", content = @Content),
    })
    @PostMapping("/{id}/kyc/approve")
    public ResponseEntity<CustomerResponse> approveKyc(
            @Parameter(description = "Müşteri UUID'si", required = true)
            @PathVariable UUID id) {

        CustomerResponse response = mediator.send(new ApproveKycCommand(id));
        return ResponseEntity.ok(response);
    }
}
