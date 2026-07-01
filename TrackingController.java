package com.example.analytics.controller;

import com.example.analytics.constants.AppConstants;
import com.example.analytics.dto.ApiResponse;
import com.example.analytics.dto.EventResponse;
import com.example.analytics.dto.TrackEventRequest;
import com.example.analytics.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Tracking", description = "Track user behavioral events")
public class TrackingController {

    private final TrackingService trackingService;

    @Operation(summary = "Track a user event",
            description = "Persists an event, auto-creates the user if new, and generates an embedding for it.")
    @PostMapping("/track")
    public ResponseEntity<ApiResponse<EventResponse>> track(@Valid @RequestBody TrackEventRequest request) {
        log.info("POST /track called for userId={} event={}", request.getUserId(), request.getEvent());
        EventResponse response = trackingService.trackEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.SUCCESS_MESSAGE_TRACK, response));
    }
}
