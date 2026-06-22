package com.turkcell.customer_service.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "file_ref", length = 255, nullable = false)
    private String fileRef;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------
    public Document() {}

    public Document(UUID id, Customer customer, String type, String fileRef, OffsetDateTime verifiedAt) {
        this.id = id;
        this.customer = customer;
        this.type = type;
        this.fileRef = fileRef;
        this.verifiedAt = verifiedAt;
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFileRef() { return fileRef; }
    public void setFileRef(String fileRef) { this.fileRef = fileRef; }

    public OffsetDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(OffsetDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
}
