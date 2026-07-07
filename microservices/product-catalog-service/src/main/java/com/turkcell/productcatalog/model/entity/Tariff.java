package com.turkcell.productcatalog.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tariffs")
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    private UUID id;

    @Column(length = 50, unique = true, nullable = false)
    private String code;

    @Column(length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TariffType type;

    @Column(name = "monthly_fee", precision = 10, scale = 2)
    private BigDecimal monthlyFee;

    @Column(name = "minutes_included")
    private Integer minutesIncluded;

    @Column(name = "sms_included")
    private Integer smsIncluded;

    @Column(name = "data_mb_included")
    private Integer dataMbIncluded;

    @Column(length = 20)
    private String status;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "tariff_addons",
        joinColumns = @JoinColumn(name = "tariff_id"),
        inverseJoinColumns = @JoinColumn(name = "addon_id")
    )
    private Set<Addon> addons = new HashSet<>();

    public Tariff() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TariffType getType() { return type; }
    public void setType(TariffType type) { this.type = type; }

    public BigDecimal getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(BigDecimal monthlyFee) { this.monthlyFee = monthlyFee; }

    public Integer getMinutesIncluded() { return minutesIncluded; }
    public void setMinutesIncluded(Integer minutesIncluded) { this.minutesIncluded = minutesIncluded; }

    public Integer getSmsIncluded() { return smsIncluded; }
    public void setSmsIncluded(Integer smsIncluded) { this.smsIncluded = smsIncluded; }

    public Integer getDataMbIncluded() { return dataMbIncluded; }
    public void setDataMbIncluded(Integer dataMbIncluded) { this.dataMbIncluded = dataMbIncluded; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public Set<Addon> getAddons() { return addons; }
    public void setAddons(Set<Addon> addons) { this.addons = addons; }

    public void addAddon(Addon addon) {
        this.addons.add(addon);
        addon.getTariffs().add(this);
    }

    public void removeAddon(Addon addon) {
        this.addons.remove(addon);
        addon.getTariffs().remove(this);
    }
}
