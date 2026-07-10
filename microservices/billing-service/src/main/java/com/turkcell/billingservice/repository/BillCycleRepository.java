package com.turkcell.billingservice.repository;

import com.turkcell.billingservice.model.entity.BillCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BillCycleRepository extends JpaRepository<BillCycle, UUID> {
    // Find all bill cycles that are due for a run (nextRunDate <= today)
    List<BillCycle> findByNextRunDateBeforeOrNextRunDateEquals(LocalDate date1, LocalDate date2);
    
    List<BillCycle> findByCustomerId(UUID customerId);
}
