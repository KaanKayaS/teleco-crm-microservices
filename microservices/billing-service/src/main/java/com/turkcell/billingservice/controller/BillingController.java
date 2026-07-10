package com.turkcell.billingservice.controller;

import com.turkcell.billingservice.model.entity.Invoice;
import com.turkcell.billingservice.service.BillingService;
import com.turkcell.billingservice.service.InvoicePdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Faturalandırma (Billing)", description = "Fatura hesaplama ve fatura döngüsü API'leri")
public class BillingController {

    private final BillingService billingService;
    private final InvoicePdfService invoicePdfService;

    public BillingController(BillingService billingService, InvoicePdfService invoicePdfService) {
        this.billingService = billingService;
        this.invoicePdfService = invoicePdfService;
    }

    @Operation(summary = "Müşterinin Faturalarını Getir", description = "Verilen Müşteri ID'sine ait tüm faturaları listeler.")
    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getInvoicesByCustomerId(@RequestParam UUID customerId) {
        return ResponseEntity.ok(billingService.getInvoicesByCustomerId(customerId));
    }

    @Operation(summary = "Fatura Detayı Getir", description = "Fatura ID'sine göre tek bir faturanın detaylarını getirir.")
    @GetMapping("/invoices/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable UUID id) {
        return billingService.getInvoice(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Faturayı PDF olarak indir", description = "Fatura ID'sine göre faturanın PDF dökümünü byte dizisi olarak döner.")
    @GetMapping(value = "/invoices/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable UUID id) {
        return billingService.getInvoice(id)
                .map(invoice -> {
                    byte[] pdfBytes = invoicePdfService.generatePdf(invoice);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + id + ".pdf")
                            .body(pdfBytes);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Manuel Fatura Döngüsünü Başlat", description = "Admin yetkisiyle aylık faturalandırma (bill-run) sürecini tetikler.")
    @PostMapping("/billing/runs")
    public ResponseEntity<String> triggerBillRun() {
        billingService.runBillingCycle();
        return ResponseEntity.ok("Fatura döngüsü başarıyla tamamlandı. Yeni kesilen faturaları görmek için GET /api/v1/invoices endpoint'ini kullanabilirsiniz.");
    }

    @Operation(summary = "Manuel Fatura Döngüsü (Cycle) Oluştur", description = "Test veya admin işlemleri için manuel abone döngüsü oluşturur.")
    @PostMapping("/billing/cycles")
    public ResponseEntity<Void> createBillCycle(@RequestParam UUID customerId) {
        billingService.createBillCycle(customerId);
        return ResponseEntity.ok().build();
    }
}
