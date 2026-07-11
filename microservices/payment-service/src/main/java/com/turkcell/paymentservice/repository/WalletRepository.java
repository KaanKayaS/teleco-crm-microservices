package com.turkcell.paymentservice.repository;

import com.turkcell.paymentservice.model.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {
    Optional<Wallet> findByCustomerId(String customerId);
}
