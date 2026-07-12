package com.turkcell.ticketservice.repository;

import com.turkcell.ticketservice.model.entity.Ticket;
import com.turkcell.ticketservice.model.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Ticket> findByStatus(TicketStatus status);

    /**
     * Finds all open/in-progress tickets whose SLA deadline has passed
     * and haven't been flagged as breached yet — used by the SLA scheduler.
     */
    @Query("SELECT t FROM Ticket t WHERE t.status IN ('OPEN', 'IN_PROGRESS') " +
           "AND t.slaDueAt < :now AND t.slaBreached = false")
    List<Ticket> findUnbreachedOverdueTickets(@Param("now") ZonedDateTime now);
}
