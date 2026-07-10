package com.turkcell.usageservice.repository;

import com.turkcell.usageservice.model.entity.Quota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface QuotaRepository extends JpaRepository<Quota, UUID> {

    @Query("SELECT q FROM Quota q WHERE q.subscriptionId = :subscriptionId " +
           "AND q.periodStart <= :now AND q.periodEnd >= :now")
    Optional<Quota> findActiveQuotaBySubscriptionId(
            @Param("subscriptionId") UUID subscriptionId,
            @Param("now") LocalDateTime now
    );
}
