package com.turkcell.usageservice.service;

import com.turkcell.usageservice.messaging.UsageEventPublisher;
import com.turkcell.usageservice.model.dto.CdrRecordedEvent;
import com.turkcell.usageservice.model.dto.QuotaExceededEvent;
import com.turkcell.usageservice.model.dto.QuotaThresholdReachedEvent;
import com.turkcell.usageservice.model.entity.Quota;
import com.turkcell.usageservice.model.entity.UsageRecord;
import com.turkcell.usageservice.model.entity.UsageType;
import com.turkcell.usageservice.repository.QuotaRepository;
import com.turkcell.usageservice.repository.UsageRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UsageService {

    private final QuotaRepository quotaRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final UsageEventPublisher eventPublisher;

    public UsageService(QuotaRepository quotaRepository,
                        UsageRecordRepository usageRecordRepository,
                        UsageEventPublisher eventPublisher) {
        this.quotaRepository = quotaRepository;
        this.usageRecordRepository = usageRecordRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void processCdr(CdrRecordedEvent cdrEvent) {
        if (usageRecordRepository.existsByCdrRef(cdrEvent.getCdrRef())) {
            // Idempotency check: Already processed this CDR
            return;
        }

        UUID subId = UUID.fromString(cdrEvent.getSubscriptionId());
        
        // Find active quota for this subscription
        Quota quota = quotaRepository.findActiveQuotaBySubscriptionId(subId, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("No active quota found for subscription " + subId));

        UsageType type = UsageType.valueOf(cdrEvent.getType().toUpperCase());
        int quantity = cdrEvent.getQuantity();

        // Update remaining quotas
        int remaining = 0;
        if (type == UsageType.VOICE) {
            quota.setMinutesRemaining(Math.max(0, quota.getMinutesRemaining() - quantity));
            remaining = quota.getMinutesRemaining();
        } else if (type == UsageType.SMS) {
            quota.setSmsRemaining(Math.max(0, quota.getSmsRemaining() - quantity));
            remaining = quota.getSmsRemaining();
        } else if (type == UsageType.DATA) {
            quota.setMbRemaining(Math.max(0, quota.getMbRemaining() - quantity));
            remaining = quota.getMbRemaining();
        }

        quotaRepository.save(quota);

        // Record the usage
        UsageRecord record = new UsageRecord();
        record.setSubscriptionId(subId);
        record.setCdrRef(cdrEvent.getCdrRef());
        record.setType(type);
        record.setQuantity(quantity);
        record.setRecordedAt(LocalDateTime.parse(cdrEvent.getTimestamp()));
        usageRecordRepository.save(record);

        // Publish events if necessary
        if (remaining == 0) {
            eventPublisher.publishQuotaExceeded(new QuotaExceededEvent(
                    subId, 
                    type.name(), 
                    type.name() + " quota has been exceeded."
            ));
        } else if (remaining <= 50) { // e.g., 50 threshold for demonstration
            eventPublisher.publishThresholdReached(new QuotaThresholdReachedEvent(
                    subId, 
                    type.name(), 
                    remaining, 
                    type.name() + " quota is running low."
            ));
        }
    }

    @Transactional
    public void initializeQuotaForSubscription(UUID subscriptionId) {
        // Here we could fetch limits from product-catalog-service or use defaults.
        // Using defaults for now (1000 Mins, 1000 SMS, 10000 MB)
        Quota quota = new Quota();
        quota.setSubscriptionId(subscriptionId);
        quota.setPeriodStart(LocalDateTime.now());
        quota.setPeriodEnd(LocalDateTime.now().plusMonths(1));
        quota.setMinutesRemaining(1000);
        quota.setSmsRemaining(1000);
        quota.setMbRemaining(10000); // 10 GB
        quotaRepository.save(quota);
    }
}
