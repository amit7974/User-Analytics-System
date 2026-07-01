package com.example.analytics.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.analytics.exception.InternalServerException;
import org.springframework.stereotype.Component;

/**
 * Converts between a {@code double[]} embedding vector and its JSON text
 * representation, used because vectors are persisted as JSON columns
 * (in lieu of a dedicated vector database column type).
 */
@Component
public class JsonVectorConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(double[] vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (Exception e) {
            throw new InternalServerException("Failed to serialize embedding vector", e);
        }
    }

    public double[] fromJson(String json) {
        try {
            return objectMapper.readValue(json, double[].class);
        } catch (Exception e) {
            throw new InternalServerException("Failed to deserialize embedding vector", e);
        }
    }
}
