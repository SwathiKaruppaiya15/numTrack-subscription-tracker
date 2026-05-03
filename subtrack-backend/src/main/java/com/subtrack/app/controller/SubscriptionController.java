package com.subtrack.app.controller;

import com.subtrack.app.dto.request.SubscriptionRequest;
import com.subtrack.app.dto.response.SubscriptionResponse;
import com.subtrack.app.service.SubscriptionService;
import com.subtrack.app.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /** POST /api/subscriptions — Create */
    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.create(SecurityUtils.getCurrentUserId(), request));
    }

    /** GET /api/subscriptions — List all */
    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getAll() {
        return ResponseEntity.ok(subscriptionService.getAllByUser(SecurityUtils.getCurrentUserId()));
    }

    /** GET /api/subscriptions/{id} — Get one */
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.getById(SecurityUtils.getCurrentUserId(), id));
    }

    /** PUT /api/subscriptions/{id} — Update */
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.update(SecurityUtils.getCurrentUserId(), id, request));
    }

    /** PATCH /api/subscriptions/{id}/cancel — Cancel */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.cancel(SecurityUtils.getCurrentUserId(), id));
    }

    /** DELETE /api/subscriptions/{id} — Delete */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        subscriptionService.delete(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
