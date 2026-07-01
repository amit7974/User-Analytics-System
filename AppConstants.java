package com.example.analytics.constants;

/**
 * Centralized application-wide constants. Keeping these in one place avoids
 * "magic numbers/strings" scattered across the codebase.
 */
public final class AppConstants {

    private AppConstants() {
        // utility class, prevent instantiation
    }

    /** Dimensionality used for all generated embedding vectors. */
    public static final int EMBEDDING_DIMENSIONS = 128;

    /** Name reported for the embedding model currently in use. */
    public static final String EMBEDDING_MODEL_NAME = "fake-embedding-v1";

    /** Default number of results returned by /search and /similar-users. */
    public static final int DEFAULT_TOP_K = 5;

    public static final String SUCCESS_MESSAGE_TRACK = "Event tracked successfully";
    public static final String SUCCESS_MESSAGE_ANALYTICS = "Analytics fetched successfully";
    public static final String SUCCESS_MESSAGE_SEARCH = "Search completed successfully";
    public static final String SUCCESS_MESSAGE_SIMILAR_USERS = "Similar users fetched successfully";
}
