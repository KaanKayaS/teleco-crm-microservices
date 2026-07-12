package com.turkcell.ticketservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TicketStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Agent/staff UUID who is handling the ticket */
    private UUID assignedTo;

    /** SLA deadline computed at creation from priority */
    @Column(nullable = false)
    private ZonedDateTime slaDueAt;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    private ZonedDateTime resolvedAt;

    /** Flag set by the SLA checker scheduler to avoid duplicate events */
    @Column(nullable = false)
    private boolean slaBreached = false;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketComment> comments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        if (status == null) {
            status = TicketStatus.OPEN;
        }
    }
}
