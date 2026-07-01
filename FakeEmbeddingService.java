package com.example.analytics.service.impl;

import com.example.analytics.constants.AppConstants;
import com.example.analytics.service.EmbeddingService;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Deterministic, dependency-free embedding generator used as a stand-in for
 * a real AI embedding model (e.g. OpenAI's text-embedding-3-small).
 * <p>
 * Algorithm: each lowercased word in the text is hashed into one of
 * {@code AppConstants.EMBEDDING_DIMENSIONS} buckets (similar to the
 * "hashing trick" / bag-of-words feature hashing used in classic NLP),
 * and the bucket's weight is incremented. The resulting vector is then
 * L2-normalized so vectors of differently-sized texts remain comparable
 * via cosine similarity.
 * <p>
 * Because the hashing is deterministic, semantically similar phrases that
 * share words (e.g. "user viewed pricing page" and "user visited pricing")
 * naturally land closer together in vector space than unrelated phrases —
 * giving a reasonable, swappable approximation of real semantic embeddings.
 */
@Service
public class FakeEmbeddingService implements EmbeddingService {

    @Override
    public double[] generateEmbedding(String text) {
        double[] vector = new double[AppConstants.EMBEDDING_DIMENSIONS];
        if (text == null || text.isBlank()) {
            return vector;
        }

        String[] words = text.toLowerCase(Locale.ROOT).trim().split("\\s+");
        for (String word : words) {
            int bucket = Math.floorMod(stableHash(word), AppConstants.EMBEDDING_DIMENSIONS);
            vector[bucket] += 1.0;
        }

        return normalize(vector);
    }

    @Override
    public String modelName() {
        return AppConstants.EMBEDDING_MODEL_NAME;
    }

    /**
     * A stable (JVM-restart-safe) string hash. {@code String.hashCode()} is
     * guaranteed stable within a single run but we implement our own simple
     * polynomial hash to keep the algorithm fully self-contained and explainable.
     */
    private int stableHash(String word) {
        int hash = 7;
        for (int i = 0; i < word.length(); i++) {
            hash = 31 * hash + word.charAt(i);
        }
        return hash;
    }

    private double[] normalize(double[] vector) {
        double sumOfSquares = 0.0;
        for (double v : vector) {
            sumOfSquares += v * v;
        }
        double magnitude = Math.sqrt(sumOfSquares);
        if (magnitude == 0.0) {
            return vector;
        }
        double[] normalized = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / magnitude;
        }
        return normalized;
    }
}
