package com.turkcell.billingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.turkcell.billingservice.client.ProductCatalogClient;
import com.turkcell.billingservice.client.TariffDto;
import com.turkcell.billingservice.model.entity.*;
import com.turkcell.billingservice.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final BillCycleRepository billCycleRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final BillingSubscriptionRepository billingSubscriptionRepository;
    private final ProductCatalogClient productCatalogClient;
    private final ObjectMapper objectMapper;

    public BillingService(InvoiceRepository invoiceRepository, 
                          BillCycleRepository billCycleRepository,
                          OutboxEventRepository outboxEventRepository,
                          BillingSubscriptionRepository billingSubscriptionRepository,
                          ProductCatalogClient productCatalogClient) {
        this.invoiceRepository = invoiceRepository;
        this.billCycleRepository = billCycleRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.billingSubscriptionRepository = billingSubscriptionRepository;
        this.productCatalogClient = productCatalogClient;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public List<Invoice> getInvoicesByCustomerId(UUID customerId) {
        return invoiceRepository.findByCustomerId(customerId);
    }

    public Optional<Invoice> getInvoice(UUID id) {
        return invoiceRepository.findById(id);
    }

    @Transactional
    public void createBillCycle(UUID customerId) {
        BillCycle cycle = new BillCycle();
        cycle.setCustomerId(customerId);
        cycle.setDayOfMonth(LocalDate.now().getDayOfMonth());
        cycle.setNextRunDate(LocalDate.now());
        billCycleRepository.save(cycle);
    }
    
    @Transactional
    public void handleSubscriptionActivated(UUID customerId, UUID subscriptionId, String tariffCode) {
        BillingSubscription sub = new BillingSubscription();
        sub.setCustomerId(customerId);
        sub.setSubscriptionId(subscriptionId);
        sub.setTariffCode(tariffCode);
        sub.setStatus("ACTIVE");
        billingSubscriptionRepository.save(sub);

        List<BillCycle> cycles = billCycleRepository.findByCustomerId(customerId);
        if (cycles.isEmpty()) {
            createBillCycle(customerId);
        }
    }

    @Transactional
    public void runBillingCycle() {
        LocalDate today = LocalDate.now();
        List<BillCycle> dueCycles = billCycleRepository.findByNextRunDateBeforeOrNextRunDateEquals(today, today);

        for (BillCycle cycle : dueCycles) {
            generateInvoiceForCycle(cycle);
        }
    }

    private void generateInvoiceForCycle(BillCycle cycle) {
        List<BillingSubscription> activeSubscriptions = billingSubscriptionRepository.findByCustomerIdAndStatus(cycle.getCustomerId(), "ACTIVE");
        
        if (activeSubscriptions.isEmpty()) {
            // Cycle atla, kesilecek fatura yok
            cycle.setNextRunDate(cycle.getNextRunDate().plusMonths(1));
            billCycleRepository.save(cycle);
            return;
        }

        Invoice invoice = new Invoice();
        invoice.setCustomerId(cycle.getCustomerId());
        // Bir faturaya bir veya birden çok abonelik yansıyabilir, ilk abonelik id'sini örnek verelim.
        invoice.setSubscriptionId(activeSubscriptions.get(0).getSubscriptionId());
        invoice.setPeriodStart(cycle.getNextRunDate().minusMonths(1));
        invoice.setPeriodEnd(cycle.getNextRunDate());
        
        BigDecimal subTotal = BigDecimal.ZERO;

        for (BillingSubscription sub : activeSubscriptions) {
            BigDecimal price = BigDecimal.ZERO;
            try {
                Optional<TariffDto> tariffOpt = productCatalogClient.getTariffByCode(sub.getTariffCode());
                if (tariffOpt.isPresent()) {
                    price = tariffOpt.get().getMonthlyFee();
                } else {
                    System.err.println("Tariff not found in catalog: " + sub.getTariffCode());
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch tariff price: " + e.getMessage());
            }

            InvoiceLine line = new InvoiceLine();
            line.setDescription("Aylık Paket Ücreti (" + sub.getTariffCode() + ")");
            line.setQuantity(BigDecimal.ONE);
            line.setUnitPrice(price != null ? price : BigDecimal.ZERO);
            line.setLineTotal(price != null ? price : BigDecimal.ZERO);
            invoice.addLine(line);
            
            subTotal = subTotal.add(line.getLineTotal());
        }

        BigDecimal taxRate = new BigDecimal("0.20"); // %20 vergi
        BigDecimal tax = subTotal.multiply(taxRate);
        BigDecimal grandTotal = subTotal.add(tax);

        invoice.setSubTotal(subTotal);
        invoice.setTax(tax);
        invoice.setGrandTotal(grandTotal);
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setDueDate(cycle.getNextRunDate().plusDays(14));
        invoice.setIssuedAt(ZonedDateTime.now());

        Invoice saved = invoiceRepository.save(invoice);
        
        cycle.setNextRunDate(cycle.getNextRunDate().plusMonths(1));
        billCycleRepository.save(cycle);

        createOutboxEvent(saved, "InvoiceGenerated");
    }

    private void createOutboxEvent(Invoice invoice, String eventType) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("Invoice");
        event.setAggregateId(invoice.getId().toString());
        event.setType(eventType);
        event.setCreatedAt(ZonedDateTime.now());
        event.setStatus("PENDING");
        
        try {
            event.setPayload(objectMapper.writeValueAsString(invoice));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization failed", e);
        }
        
        outboxEventRepository.save(event);
    }
}
