package com.turkcell.customer_service.repository;

import com.turkcell.customer_service.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findAllByCustomerId(UUID customerId);
}
