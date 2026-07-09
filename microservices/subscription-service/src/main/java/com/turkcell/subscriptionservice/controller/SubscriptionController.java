package com.turkcell.subscriptionservice.controller;

import com.turkcell.subscriptionservice.dto.CreateSubscriptionRequest;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;
import com.turkcell.subscriptionservice.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription", description = "Subscription state machine & MSISDN allocation APIs")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * POST /api/v1/subscriptions
     * Internal endpoint — called by Order Service after OrderConfirmed.
     * Allocates a MSISDN and activates the subscription.
     */
    @PostMapping
    @Operation(summary = "Create subscription (internal)", description = "Creates a new subscription and allocates a MSISDN. Called internally by Order Service.")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/subscriptions/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get subscription by ID")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable UUID id) {
        return subscriptionService.getSubscription(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/subscriptions/{id}/suspend
     * Transitions: ACTIVE → SUSPENDED
     */
    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend subscription", description = "Transitions subscription from ACTIVE to SUSPENDED.")
    public ResponseEntity<SubscriptionResponse> suspendSubscription(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(subscriptionService.suspendSubscription(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/v1/subscriptions/{id}/reactivate
     * Transitions: SUSPENDED → ACTIVE
     */
    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate subscription", description = "Transitions subscription from SUSPENDED to ACTIVE.")
    public ResponseEntity<SubscriptionResponse> reactivateSubscription(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(subscriptionService.reactivateSubscription(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/v1/subscriptions/{id}/terminate
     * Transitions: ACTIVE|SUSPENDED → TERMINATED, releases MSISDN.
     */
    @PostMapping("/{id}/terminate")
    @Operation(summary = "Terminate subscription", description = "Terminates the subscription and releases the MSISDN back to the pool.")
    public ResponseEntity<SubscriptionResponse> terminateSubscription(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(subscriptionService.terminateSubscription(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
