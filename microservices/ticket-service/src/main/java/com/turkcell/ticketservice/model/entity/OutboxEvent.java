package com.turkcell.ticketservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    private String aggregateType;

    @Column(length = 100)
    private String aggregateId;

    @Column(length = 100)
    private String type;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    private ZonedDateTime createdAt;
    private ZonedDateTime processedAt;

    @Column(length = 20)
    private String status; // PENDING, PROCESSED, FAILED
}
