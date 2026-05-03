package com.subtrack.app.controller;

import com.subtrack.app.dto.request.PaymentRequest;
import com.subtrack.app.dto.response.PaymentResponse;
import com.subtrack.app.service.PaymentService;
import com.subtrack.app.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /** POST /api/payments — Record a payment */
    @PostMapping
    public ResponseEntity<PaymentResponse> record(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.recordPayment(SecurityUtils.getCurrentUserId(), request));
    }

    /** GET /api/payments — All payments for current user */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(paymentService.getByUser(SecurityUtils.getCurrentUserId()));
    }

    /** GET /api/payments/subscription/{subscriptionId} */
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<PaymentResponse>> getBySubscription(@PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(
                paymentService.getBySubscription(SecurityUtils.getCurrentUserId(), subscriptionId));
    }
}
