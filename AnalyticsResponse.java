package com.example.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Aggregated analytics payload returned by {@code GET /analytics}.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    private long totalEvents;
    private long totalUsers;
    private Map<String, Long> eventsPerUser;
    private List<TopUser> topActiveUsers;
    private Map<String, Long> eventCounts;
    private List<EventResponse> latestEvents;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopUser {
        private String userId;
        private long totalEvents;
    }
}
