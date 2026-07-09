package com.turkcell.subscriptionservice.service;

import com.turkcell.subscriptionservice.dto.CreateSubscriptionRequest;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;
import com.turkcell.subscriptionservice.messaging.producer.SubscriptionEventProducer;
import com.turkcell.subscriptionservice.model.entity.MsisdnPool;
import com.turkcell.subscriptionservice.model.entity.MsisdnStatus;
import com.turkcell.subscriptionservice.model.entity.Subscription;
import com.turkcell.subscriptionservice.model.entity.SubscriptionStatus;
import com.turkcell.subscriptionservice.repository.MsisdnPoolRepository;
import com.turkcell.subscriptionservice.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MsisdnPoolRepository msisdnPoolRepository;
    private final SubscriptionEventProducer eventProducer;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               MsisdnPoolRepository msisdnPoolRepository,
                               SubscriptionEventProducer eventProducer) {
        this.subscriptionRepository = subscriptionRepository;
        this.msisdnPoolRepository = msisdnPoolRepository;
        this.eventProducer = eventProducer;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    /**
     * Creates a new subscription by allocating a FREE MSISDN from the pool.
     * Called internally by the Order Service after OrderConfirmed event.
     */
    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        // 1. Allocate a MSISDN from the pool
        MsisdnPool msisdnEntry = msisdnPoolRepository.findFirstFreeMsisdn()
                .orElseThrow(() -> new IllegalStateException("No FREE MSISDN available in the pool"));

        msisdnEntry.setStatus(MsisdnStatus.ALLOCATED);
        msisdnEntry.setReservedUntil(null);
        msisdnPoolRepository.save(msisdnEntry);

        // 2. Create the subscription
        Subscription subscription = new Subscription();
        subscription.setCustomerId(request.getCustomerId());
        subscription.setMsisdn(msisdnEntry.getMsisdn());
        subscription.setTariffCode(request.getTariffCode());
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        Subscription saved = subscriptionRepository.save(subscription);

        // 3. Publish SubscriptionActivated event
        eventProducer.publishSubscriptionActivated(saved);

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    public Optional<SubscriptionResponse> getSubscription(UUID id) {
        return subscriptionRepository.findById(id).map(this::toResponse);
    }

    // -------------------------------------------------------------------------
    // STATE TRANSITIONS
    // -------------------------------------------------------------------------

    /**
     * Transitions ACTIVE → SUSPENDED.
     */
    @Transactional
    public SubscriptionResponse suspendSubscription(UUID id) {
        Subscription subscription = findActiveOrThrow(id, SubscriptionStatus.ACTIVE, "suspend");
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        Subscription saved = subscriptionRepository.save(subscription);
        eventProducer.publishSubscriptionSuspended(saved);
        return toResponse(saved);
    }

    /**
     * Transitions SUSPENDED → ACTIVE.
     */
    @Transactional
    public SubscriptionResponse reactivateSubscription(UUID id) {
        Subscription subscription = findActiveOrThrow(id, SubscriptionStatus.SUSPENDED, "reactivate");
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        Subscription saved = subscriptionRepository.save(subscription);
        eventProducer.publishSubscriptionActivated(saved);
        return toResponse(saved);
    }

    /**
     * Transitions ACTIVE|SUSPENDED → TERMINATED and releases the MSISDN.
     */
    @Transactional
    public SubscriptionResponse terminateSubscription(UUID id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + id));

        if (subscription.getStatus() == SubscriptionStatus.TERMINATED) {
            throw new IllegalStateException("Subscription is already TERMINATED");
        }

        subscription.setStatus(SubscriptionStatus.TERMINATED);
        subscription.setTerminatedAt(OffsetDateTime.now());
        Subscription saved = subscriptionRepository.save(subscription);

        // Release the MSISDN back to the pool
        releaseMsisdn(saved.getMsisdn());

        eventProducer.publishSubscriptionTerminated(saved);
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Subscription findActiveOrThrow(UUID id, SubscriptionStatus required, String action) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + id));
        if (subscription.getStatus() != required) {
            throw new IllegalStateException(
                    "Cannot " + action + " subscription in status: " + subscription.getStatus());
        }
        return subscription;
    }

    private void releaseMsisdn(String msisdn) {
        msisdnPoolRepository.findById(msisdn).ifPresent(entry -> {
            entry.setStatus(MsisdnStatus.FREE);
            entry.setReservedUntil(null);
            msisdnPoolRepository.save(entry);
        });
    }

    private SubscriptionResponse toResponse(Subscription s) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(s.getId());
        response.setCustomerId(s.getCustomerId());
        response.setMsisdn(s.getMsisdn());
        response.setTariffCode(s.getTariffCode());
        response.setStatus(s.getStatus());
        response.setActivatedAt(s.getActivatedAt());
        response.setTerminatedAt(s.getTerminatedAt());
        return response;
    }
}
