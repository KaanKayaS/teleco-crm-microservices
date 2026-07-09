package com.turkcell.subscriptionservice.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "msisdn_pool")
public class MsisdnPool {

    @Id
    @Column(name = "msisdn", nullable = false, length = 20)
    private String msisdn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MsisdnStatus status;

    @Column(name = "reserved_until")
    private OffsetDateTime reservedUntil;

    public MsisdnPool() {}

    // --- Getters & Setters ---

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public MsisdnStatus getStatus() {
        return status;
    }

    public void setStatus(MsisdnStatus status) {
        this.status = status;
    }

    public OffsetDateTime getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(OffsetDateTime reservedUntil) {
        this.reservedUntil = reservedUntil;
    }
}
