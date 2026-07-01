package com.example.analytics.service.impl;

import com.example.analytics.dto.SearchResultResponse;
import com.example.analytics.entity.Embedding;
import com.example.analytics.entity.Event;
import com.example.analytics.exception.BadRequestException;
import com.example.analytics.repository.EmbeddingRepository;
import com.example.analytics.repository.EventRepository;
import com.example.analytics.service.EmbeddingService;
import com.example.analytics.service.SearchService;
import com.example.analytics.util.JsonVectorConverter;
import com.example.analytics.util.VectorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements naive (brute-force) semantic search: embeds the query, then
 * scores every stored event embedding via cosine similarity and returns the
 * top-K matches. This is O(n) per query, which is acceptable for the
 * dataset sizes in this assignment; see README for how this would scale
 * using a real ANN vector index in production.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final EmbeddingService embeddingService;
    private final EmbeddingRepository embeddingRepository;
    private final EventRepository eventRepository;
    private final JsonVectorConverter jsonVectorConverter;

    @Override
    public List<SearchResultResponse> search(String query, int topK) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("query parameter must not be blank");
        }

        long start = System.currentTimeMillis();
        double[] queryVector = embeddingService.generateEmbedding(query);

        List<Embedding> allEmbeddings = embeddingRepository.findAll();
        if (allEmbeddings.isEmpty()) {
            return List.of();
        }

        Map<Long, Event> eventsById = new HashMap<>();
        eventRepository.findAllById(allEmbeddings.stream().map(Embedding::getEventId).toList())
                .forEach(e -> eventsById.put(e.getId(), e));

        List<SearchResultResponse> results = allEmbeddings.stream()
                .map(embedding -> {
                    double[] vector = jsonVectorConverter.fromJson(embedding.getVector());
                    double score = VectorUtils.cosineSimilarity(queryVector, vector);
                    Event event = eventsById.get(embedding.getEventId());
                    if (event == null) {
                        return null;
                    }
                    return SearchResultResponse.builder()
                            .eventId(event.getId())
                            .userId(event.getUserId())
                            .event(event.getEvent())
                            .metadata(event.getMetadata())
                            .score(score)
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparingDouble(SearchResultResponse::getScore).reversed())
                .limit(topK)
                .toList();

        log.info("Search for query='{}' returned {} results in {} ms",
                query, results.size(), System.currentTimeMillis() - start);
        return results;
    }
}
