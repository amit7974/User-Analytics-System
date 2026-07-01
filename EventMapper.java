package com.example.analytics.mapper;

import com.example.analytics.dto.EventResponse;
import com.example.analytics.entity.Event;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link Event} entities and their API-facing DTOs.
 */
@Component
public class EventMapper {

    public EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .userId(event.getUserId())
                .event(event.getEvent())
                .metadata(event.getMetadata())
                .timestamp(event.getTimestamp())
                .embeddingId(event.getEmbeddingId())
                .build();
    }
}
