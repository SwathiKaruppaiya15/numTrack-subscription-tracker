package com.subtrack.app.controller;

import com.subtrack.app.dto.response.AnalyticsResponse;
import com.subtrack.app.dto.response.InsightResponse;
import com.subtrack.app.service.AnalyticsService;
import com.subtrack.app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /** GET /api/analytics — Full analytics dashboard */
    @GetMapping
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getAnalytics(SecurityUtils.getCurrentUserId()));
    }

    /** GET /api/analytics/insights — Smart insights */
    @GetMapping("/insights")
    public ResponseEntity<InsightResponse> getInsights() {
        return ResponseEntity.ok(analyticsService.getInsights(SecurityUtils.getCurrentUserId()));
    }
}
