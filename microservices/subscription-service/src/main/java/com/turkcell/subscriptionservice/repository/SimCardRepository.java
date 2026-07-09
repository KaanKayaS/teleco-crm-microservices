package com.turkcell.subscriptionservice.repository;

import com.turkcell.subscriptionservice.model.entity.SimCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimCardRepository extends JpaRepository<SimCard, String> {

    Optional<SimCard> findByMsisdn(String msisdn);
}
