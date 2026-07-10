package com.turkcell.usageservice.repository;

import com.turkcell.usageservice.model.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {
    
    List<UsageRecord> findBySubscriptionIdAndRecordedAtBetweenOrderByRecordedAtDesc(
            UUID subscriptionId, 
            LocalDateTime from, 
            LocalDateTime to
    );
    
    boolean existsByCdrRef(String cdrRef);
}
