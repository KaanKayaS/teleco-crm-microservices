package com.turkcell.ticketservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.turkcell.ticketservice.dto.request.AddCommentRequest;
import com.turkcell.ticketservice.dto.request.AssignTicketRequest;
import com.turkcell.ticketservice.dto.request.CreateTicketRequest;
import com.turkcell.ticketservice.dto.response.TicketCommentResponse;
import com.turkcell.ticketservice.dto.response.TicketResponse;
import com.turkcell.ticketservice.model.entity.*;
import com.turkcell.ticketservice.repository.OutboxEventRepository;
import com.turkcell.ticketservice.repository.TicketCommentRepository;
import com.turkcell.ticketservice.repository.TicketRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final SlaService slaService;
    private final ObjectMapper objectMapper;

    public TicketService(TicketRepository ticketRepository,
                         TicketCommentRepository commentRepository,
                         OutboxEventRepository outboxEventRepository,
                         SlaService slaService) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.slaService = slaService;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ----------------------------------------------------------------
    // POST /api/v1/tickets
    // FR-31: Müşteriler şikayet, talep ve arıza kaydı açabilmelidir.
    // FR-32: Ticket otomatik olarak SLA bazlı atanır.
    // FR-33: Açıldığında müşteriye bildirim gider (event publish).
    // ----------------------------------------------------------------
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId(request.getCustomerId());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());
        ticket.setDescription(request.getDescription());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setSlaDueAt(slaService.computeSlaDueAt(request.getPriority()));

        Ticket saved = ticketRepository.save(ticket);
        createOutboxEvent(saved.getId().toString(), "Ticket", "TicketOpened", saved);
        return toResponse(saved);
    }

    // ----------------------------------------------------------------
    // GET /api/v1/tickets/{id}
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public Optional<TicketResponse> getTicket(UUID id) {
        return ticketRepository.findById(id).map(this::toResponseWithComments);
    }

    // ----------------------------------------------------------------
    // GET /api/v1/tickets?customerId=...
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<TicketResponse> listByCustomer(UUID customerId) {
        return ticketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // POST /api/v1/tickets/{id}/comments
    // ----------------------------------------------------------------
    @Transactional
    public TicketCommentResponse addComment(UUID ticketId, AddCommentRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket bulunamadı: " + ticketId));

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthorId(request.getAuthorId());
        comment.setBody(request.getBody());

        TicketComment saved = commentRepository.save(comment);

        // Status → IN_PROGRESS if it was still OPEN
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            ticketRepository.save(ticket);
        }

        createOutboxEvent(ticketId.toString(), "Ticket", "TicketCommentAdded", saved);
        return toCommentResponse(saved);
    }

    // ----------------------------------------------------------------
    // POST /api/v1/tickets/{id}/assign
    // FR-32: Ticket otomatik olarak ilgili ekibe SLA bazlı atanır.
    // ----------------------------------------------------------------
    @Transactional
    public TicketResponse assignTicket(UUID ticketId, AssignTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket bulunamadı: " + ticketId));

        ticket.setAssignedTo(request.getAssignedTo());
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        Ticket saved = ticketRepository.save(ticket);
        createOutboxEvent(ticketId.toString(), "Ticket", "TicketAssigned", saved);
        return toResponse(saved);
    }

    // ----------------------------------------------------------------
    // POST /api/v1/tickets/{id}/resolve
    // ----------------------------------------------------------------
    @Transactional
    public TicketResponse resolveTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket bulunamadı: " + ticketId));

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(ZonedDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        createOutboxEvent(ticketId.toString(), "Ticket", "TicketResolved", saved);
        return toResponse(saved);
    }

    // ----------------------------------------------------------------
    // @Scheduled — SLA breach checker (every 5 min)
    // Publishes SlaBreached event for tickets past their deadline.
    // ----------------------------------------------------------------
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void checkSlaBreaches() {
        List<Ticket> overdueTickets = ticketRepository.findUnbreachedOverdueTickets(ZonedDateTime.now());
        for (Ticket ticket : overdueTickets) {
            ticket.setSlaBreached(true);
            ticketRepository.save(ticket);
            createOutboxEvent(ticket.getId().toString(), "Ticket", "SlaBreached", ticket);
            System.out.printf("[SLA-CHECK] SLA ihlali tespit edildi — ticketId=%s, slaDueAt=%s%n",
                    ticket.getId(), ticket.getSlaDueAt());
        }
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private void createOutboxEvent(String aggregateId, String aggregateType, String eventType, Object payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setType(eventType);
        event.setCreatedAt(ZonedDateTime.now());
        event.setStatus("PENDING");
        try {
            event.setPayload(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Event serializasyon hatası", e);
        }
        outboxEventRepository.save(event);
    }

    private TicketResponse toResponse(Ticket ticket) {
        TicketResponse r = new TicketResponse();
        r.setId(ticket.getId());
        r.setCustomerId(ticket.getCustomerId());
        r.setCategory(ticket.getCategory());
        r.setPriority(ticket.getPriority());
        r.setStatus(ticket.getStatus());
        r.setDescription(ticket.getDescription());
        r.setAssignedTo(ticket.getAssignedTo());
        r.setSlaDueAt(ticket.getSlaDueAt());
        r.setCreatedAt(ticket.getCreatedAt());
        r.setResolvedAt(ticket.getResolvedAt());
        r.setSlaBreached(ticket.isSlaBreached());
        return r;
    }

    private TicketResponse toResponseWithComments(Ticket ticket) {
        TicketResponse r = toResponse(ticket);
        r.setComments(commentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId())
                .stream().map(this::toCommentResponse).toList());
        return r;
    }

    private TicketCommentResponse toCommentResponse(TicketComment c) {
        TicketCommentResponse r = new TicketCommentResponse();
        r.setId(c.getId());
        r.setTicketId(c.getTicket().getId());
        r.setAuthorId(c.getAuthorId());
        r.setBody(c.getBody());
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }
}
