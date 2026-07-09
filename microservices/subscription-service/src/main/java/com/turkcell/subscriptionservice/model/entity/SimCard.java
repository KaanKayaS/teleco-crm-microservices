package com.turkcell.subscriptionservice.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sim_cards")
public class SimCard {

    @Id
    @Column(name = "iccid", nullable = false, length = 30)
    private String iccid;

    @Column(name = "imsi", nullable = false, length = 20)
    private String imsi;

    @Column(name = "msisdn", nullable = false, length = 20)
    private String msisdn;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    public SimCard() {}

    // --- Getters & Setters ---

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
