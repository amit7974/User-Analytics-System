package com.example.analytics.controller;

import com.example.analytics.constants.AppConstants;
import com.example.analytics.dto.AnalyticsResponse;
import com.example.analytics.dto.ApiResponse;
import com.example.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Aggregate event analytics with optional filters")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get analytics",
            description = "Returns totals, per-user counts, top active users, event counts, and latest events. "
                    + "Optionally filter by event name and/or date range.")
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> analytics(
            @RequestParam(required = false) String event,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        log.info("GET /analytics called with event={} from={} to={}", event, from, to);
        AnalyticsResponse response = analyticsService.getAnalytics(event, from, to);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.SUCCESS_MESSAGE_ANALYTICS, response));
    }
}
