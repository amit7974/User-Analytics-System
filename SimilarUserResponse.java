package com.example.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A single ranked result returned by {@code GET /similar-users}.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarUserResponse {

    private String userId;
    private double similarityScore;
    private long totalEvents;
}
