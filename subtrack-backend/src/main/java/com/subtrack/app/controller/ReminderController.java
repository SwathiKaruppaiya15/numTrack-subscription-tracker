package com.subtrack.app.controller;

import com.subtrack.app.dto.response.ReminderResponse;
import com.subtrack.app.service.ReminderService;
import com.subtrack.app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    /** GET /api/reminders — All reminders for current user */
    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getAll() {
        return ResponseEntity.ok(reminderService.getByUser(SecurityUtils.getCurrentUserId()));
    }

    /** POST /api/reminders/test/{subscriptionId} — Trigger test reminder */
    @PostMapping("/test/{subscriptionId}")
    public ResponseEntity<ReminderResponse> triggerTest(@PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(
                reminderService.triggerTest(SecurityUtils.getCurrentUserId(), subscriptionId));
    }
}
