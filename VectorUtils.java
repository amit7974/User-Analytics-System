package com.example.analytics.util;

/**
 * Pure, dependency-free vector math helpers used for similarity search.
 * Implemented from scratch (no external linear-algebra library) so the
 * algorithmic logic is fully transparent and interview-explainable.
 */
public final class VectorUtils {

    private VectorUtils() {
        // utility class
    }

    /**
     * Computes the dot product of two equal-length vectors:
     * sum(a[i] * b[i]) for all i.
     */
    public static double dotProduct(double[] a, double[] b) {
        validateSameLength(a, b);
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i]; // multiply matching components and accumulate
        }
        return sum;
    }

    /**
     * Computes the Euclidean (L2) magnitude/length of a vector:
     * sqrt(sum(x[i]^2)).
     */
    public static double magnitude(double[] vector) {
        double sumOfSquares = 0.0;
        for (double v : vector) {
            sumOfSquares += v * v; // square each component and accumulate
        }
        return Math.sqrt(sumOfSquares);
    }

    /**
     * Returns a unit-length copy of the given vector (magnitude == 1).
     * Normalization removes the influence of vector length, leaving only direction,
     * which is useful when comparing vectors of different raw scales.
     */
    public static double[] normalizeVector(double[] vector) {
        double magnitude = magnitude(vector);
        double[] normalized = new double[vector.length];
        if (magnitude == 0.0) {
            return normalized; // zero vector stays zero, avoids divide-by-zero
        }
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / magnitude; // scale each component down to unit length
        }
        return normalized;
    }

    /**
     * Computes the cosine similarity between two vectors:
     * cos(theta) = (A . B) / (||A|| * ||B||)
     * <p>
     * Result ranges from -1 (opposite) to 1 (identical direction); for our
     * non-negative word-frequency-style embeddings the practical range is [0, 1].
     * Returns 0 when either vector has zero magnitude (no meaningful direction).
     */
    public static double cosineSimilarity(double[] a, double[] b) {
        validateSameLength(a, b);
        double magnitudeA = magnitude(a);
        double magnitudeB = magnitude(b);
        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            return 0.0;
        }
        return dotProduct(a, b) / (magnitudeA * magnitudeB);
    }

    private static void validateSameLength(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "Vectors must have the same length: " + a.length + " vs " + b.length);
        }
    }
}
