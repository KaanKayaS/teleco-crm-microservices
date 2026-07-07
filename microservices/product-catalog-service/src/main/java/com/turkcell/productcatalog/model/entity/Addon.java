package com.turkcell.productcatalog.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "addons")
public class Addon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    private UUID id;

    @Column(length = 50, unique = true, nullable = false)
    private String code;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AddonType type;

    @Column(name = "validity_days")
    private Integer validityDays;

    @ManyToMany(mappedBy = "addons")
    @JsonIgnore
    private Set<Tariff> tariffs = new HashSet<>();

    public Addon() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public AddonType getType() { return type; }
    public void setType(AddonType type) { this.type = type; }

    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }

    public Set<Tariff> getTariffs() { return tariffs; }
    public void setTariffs(Set<Tariff> tariffs) { this.tariffs = tariffs; }
}
