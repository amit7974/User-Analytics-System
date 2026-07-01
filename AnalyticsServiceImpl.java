package com.example.analytics.service.impl;

import com.example.analytics.dto.AnalyticsResponse;
import com.example.analytics.entity.Event;
import com.example.analytics.mapper.EventMapper;
import com.example.analytics.repository.EventRepository;
import com.example.analytics.repository.UserRepository;
import com.example.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes aggregate analytics over tracked events, with optional filtering
 * by event name and/or date range.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final int TOP_USERS_LIMIT = 10;
    private static final int LATEST_EVENTS_LIMIT = 10;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    @Override
    public AnalyticsResponse getAnalytics(String event, LocalDate from, LocalDate to) {
        long start = System.currentTimeMillis();

        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(23, 59, 59) : null;

        long totalEvents = eventRepository.count();
        long totalUsers = userRepository.count();

        Map<String, Long> eventsPerUser = new LinkedHashMap<>();
        eventRepository.countEventsPerUser(event, fromDateTime, toDateTime)
                .forEach(row -> eventsPerUser.put(row.getUserId(), row.getTotal()));

        List<AnalyticsResponse.TopUser> topActiveUsers = eventsPerUser.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_USERS_LIMIT)
                .map(e -> AnalyticsResponse.TopUser.builder()
                        .userId(e.getKey())
                        .totalEvents(e.getValue())
                        .build())
                .toList();

        Map<String, Long> eventCounts = new LinkedHashMap<>();
        eventRepository.countByEventName(event, fromDateTime, toDateTime)
                .forEach(row -> eventCounts.put(row.getEventName(), row.getTotal()));

        List<Event> latest = eventRepository.findFiltered(event, fromDateTime, toDateTime,
                PageRequest.of(0, LATEST_EVENTS_LIMIT, Sort.by(Sort.Direction.DESC, "timestamp")));

        AnalyticsResponse response = AnalyticsResponse.builder()
                .totalEvents(totalEvents)
                .totalUsers(totalUsers)
                .eventsPerUser(eventsPerUser)
                .topActiveUsers(topActiveUsers)
                .eventCounts(eventCounts)
                .latestEvents(latest.stream().map(eventMapper::toResponse).toList())
                .build();

        log.info("Analytics generated in {} ms (event={}, from={}, to={})",
                System.currentTimeMillis() - start, event, from, to);
        return response;
    }
}
