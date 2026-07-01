package com.example.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A single ranked result returned by {@code GET /search}.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultResponse {

    private Long eventId;
    private String userId;
    private String event;
    private String metadata;
    private double score;
}
