package com.example.analytics.service;

/**
 * Abstraction over embedding generation. Implementations convert arbitrary
 * text into a fixed-length numeric vector. Swapping {@link com.example.analytics.service.impl.FakeEmbeddingService}
 * for a real provider (OpenAI, Hugging Face, etc.) requires no changes to
 * any calling business logic — only a new implementation of this interface.
 */
public interface EmbeddingService {

    /**
     * Generates a fixed-length embedding vector for the given text.
     *
     * @param text input text (event description, search query, etc.)
     * @return a numeric vector of length {@code AppConstants.EMBEDDING_DIMENSIONS}
     */
    double[] generateEmbedding(String text);

    /** Name of the underlying model, persisted alongside each embedding. */
    String modelName();
}
