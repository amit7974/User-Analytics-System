package com.example.analytics.exception;

/** Thrown when a requested resource (user, event, embedding) cannot be found. */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
