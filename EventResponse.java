package com.example.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight representation of a tracked event returned by the API.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String userId;
    private String event;
    private String metadata;
    private LocalDateTime timestamp;
    private Long embeddingId;
}
