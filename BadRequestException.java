package com.example.analytics.exception;

/** Thrown when a client request is malformed or violates business rules. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
