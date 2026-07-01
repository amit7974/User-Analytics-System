package com.example.analytics.service.impl;

import com.example.analytics.constants.AppConstants;
import com.example.analytics.dto.SimilarUserResponse;
import com.example.analytics.entity.Embedding;
import com.example.analytics.entity.Event;
import com.example.analytics.exception.BadRequestException;
import com.example.analytics.exception.EntityNotFoundException;
import com.example.analytics.repository.EmbeddingRepository;
import com.example.analytics.repository.EventRepository;
import com.example.analytics.repository.UserRepository;
import com.example.analytics.util.JsonVectorConverter;
import com.example.analytics.util.VectorUtils;
import com.example.analytics.service.SimilarUsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds users with behaviorally similar event histories by representing
 * each user as the centroid (average) of their event embeddings, then
 * ranking other users by cosine similarity against the target user's centroid.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarUsersServiceImpl implements SimilarUsersService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EmbeddingRepository embeddingRepository;
    private final JsonVectorConverter jsonVectorConverter;

    @Override
    public List<SimilarUserResponse> findSimilarUsers(String userId, int topK) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("userId parameter must not be blank");
        }
        if (!userRepository.existsByUserId(userId)) {
            throw new EntityNotFoundException("No user found with userId=" + userId);
        }

        long start = System.currentTimeMillis();

        // Build a centroid embedding per user from all of their tracked events.
        Map<String, List<Event>> eventsByUser = new HashMap<>();
        for (Event event : eventRepository.findAll()) {
            eventsByUser.computeIfAbsent(event.getUserId(), k -> new java.util.ArrayList<>()).add(event);
        }

        Map<Long, double[]> vectorsByEmbeddingId = new HashMap<>();
        for (Embedding embedding : embeddingRepository.findAll()) {
            vectorsByEmbeddingId.put(embedding.getId(), jsonVectorConverter.fromJson(embedding.getVector()));
        }

        Map<String, double[]> centroidByUser = new HashMap<>();
        for (Map.Entry<String, List<Event>> entry : eventsByUser.entrySet()) {
            double[] centroid = new double[AppConstants.EMBEDDING_DIMENSIONS];
            int count = 0;
            for (Event event : entry.getValue()) {
                double[] vector = event.getEmbeddingId() != null
                        ? vectorsByEmbeddingId.get(event.getEmbeddingId())
                        : null;
                if (vector == null) {
                    continue;
                }
                for (int i = 0; i < centroid.length; i++) {
                    centroid[i] += vector[i];
                }
                count++;
            }
            if (count > 0) {
                for (int i = 0; i < centroid.length; i++) {
                    centroid[i] /= count;
                }
            }
            centroidByUser.put(entry.getKey(), centroid);
        }

        double[] targetCentroid = centroidByUser.get(userId);
        if (targetCentroid == null) {
            return List.of();
        }

        List<SimilarUserResponse> results = centroidByUser.entrySet().stream()
                .filter(e -> !e.getKey().equals(userId))
                .map(e -> SimilarUserResponse.builder()
                        .userId(e.getKey())
                        .similarityScore(VectorUtils.cosineSimilarity(targetCentroid, e.getValue()))
                        .totalEvents(eventsByUser.getOrDefault(e.getKey(), List.of()).size())
                        .build())
                .sorted(Comparator.comparingDouble(SimilarUserResponse::getSimilarityScore).reversed())
                .limit(topK)
                .toList();

        log.info("Similar-users lookup for userId={} returned {} results in {} ms",
                userId, results.size(), System.currentTimeMillis() - start);
        return results;
    }
}
