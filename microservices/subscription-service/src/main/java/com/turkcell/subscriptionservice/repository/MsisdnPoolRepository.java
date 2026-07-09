package com.turkcell.subscriptionservice.repository;

import com.turkcell.subscriptionservice.model.entity.MsisdnPool;
import com.turkcell.subscriptionservice.model.entity.MsisdnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MsisdnPoolRepository extends JpaRepository<MsisdnPool, String> {

    /**
     * Finds the first FREE MSISDN available for allocation.
     * Uses LIMIT 1 to pick one deterministically.
     */
    @Query("SELECT m FROM MsisdnPool m WHERE m.status = 'FREE' ORDER BY m.msisdn ASC LIMIT 1")
    Optional<MsisdnPool> findFirstFreeMsisdn();

    Optional<MsisdnPool> findByMsisdnAndStatus(String msisdn, MsisdnStatus status);
}
