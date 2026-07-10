package com.turkcell.usageservice.service;

import com.turkcell.usageservice.model.entity.Quota;
import com.turkcell.usageservice.model.entity.UsageRecord;
import com.turkcell.usageservice.repository.QuotaRepository;
import com.turkcell.usageservice.repository.UsageRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UsageQueryService {

    private final QuotaRepository quotaRepository;
    private final UsageRecordRepository usageRecordRepository;

    public UsageQueryService(QuotaRepository quotaRepository, UsageRecordRepository usageRecordRepository) {
        this.quotaRepository = quotaRepository;
        this.usageRecordRepository = usageRecordRepository;
    }

    public Quota getActiveQuota(UUID subscriptionId) {
        return quotaRepository.findActiveQuotaBySubscriptionId(subscriptionId, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("No active quota found for subscription " + subscriptionId));
    }

    public List<UsageRecord> getUsageHistory(UUID subscriptionId, LocalDateTime from, LocalDateTime to) {
        return usageRecordRepository.findBySubscriptionIdAndRecordedAtBetweenOrderByRecordedAtDesc(
                subscriptionId, from, to);
    }
}
