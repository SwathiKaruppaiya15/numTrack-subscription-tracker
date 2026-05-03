package com.subtrack.app.controller;

import com.subtrack.app.dto.request.UsageLogRequest;
import com.subtrack.app.dto.response.UsageLogResponse;
import com.subtrack.app.service.UsageLogService;
import com.subtrack.app.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageLogController {

    private final UsageLogService usageLogService;

    /** POST /api/usage — Log usage */
    @PostMapping
    public ResponseEntity<UsageLogResponse> log(@Valid @RequestBody UsageLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usageLogService.logUsage(SecurityUtils.getCurrentUserId(), request));
    }

    /** GET /api/usage/subscription/{id} — Get usage logs */
    @GetMapping("/subscription/{id}")
    public ResponseEntity<List<UsageLogResponse>> getBySubscription(@PathVariable UUID id) {
        return ResponseEntity.ok(
                usageLogService.getBySubscription(SecurityUtils.getCurrentUserId(), id));
    }
}
