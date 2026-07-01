package com.example.analytics.service.impl;

import com.example.analytics.dto.EventResponse;
import com.example.analytics.dto.TrackEventRequest;
import com.example.analytics.entity.Embedding;
import com.example.analytics.entity.Event;
import com.example.analytics.entity.User;
import com.example.analytics.mapper.EventMapper;
import com.example.analytics.repository.EmbeddingRepository;
import com.example.analytics.repository.EventRepository;
import com.example.analytics.repository.UserRepository;
import com.example.analytics.service.EmbeddingService;
import com.example.analytics.service.TrackingService;
import com.example.analytics.util.JsonVectorConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Orchestrates event tracking: persists the user (creating it on first sight),
 * persists the event, generates an embedding for the event description, and
 * links the two together.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EmbeddingRepository embeddingRepository;
    private final EmbeddingService embeddingService;
    private final JsonVectorConverter jsonVectorConverter;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventResponse trackEvent(TrackEventRequest request) {
        log.info("Tracking event '{}' for userId={}", request.getEvent(), request.getUserId());

        ensureUserExists(request.getUserId());

        Event event = Event.builder()
                .userId(request.getUserId())
                .event(request.getEvent())
                .metadata(request.getMetadata() != null ? request.getMetadata().toString() : null)
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now())
                .build();
        event = eventRepository.save(event);

        double[] vector = embeddingService.generateEmbedding(request.getEvent());
        Embedding embedding = Embedding.builder()
                .eventId(event.getId())
                .vector(jsonVectorConverter.toJson(vector))
                .modelName(embeddingService.modelName())
                .build();
        embedding = embeddingRepository.save(embedding);

        event.setEmbeddingId(embedding.getId());
        event = eventRepository.save(event);

        log.info("Event {} tracked with embedding {}", event.getId(), embedding.getId());
        return eventMapper.toResponse(event);
    }

    private void ensureUserExists(String userId) {
        if (!userRepository.existsByUserId(userId)) {
            userRepository.save(User.builder().userId(userId).build());
            log.info("Created new user record for userId={}", userId);
        }
    }
}
