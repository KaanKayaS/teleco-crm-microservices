package com.turkcell.productcatalog.repository;

import com.turkcell.productcatalog.model.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, UUID> {
    Optional<Tariff> findByCode(String code);
}
