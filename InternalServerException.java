package com.example.analytics.exception;

/** Thrown for unexpected server-side failures that aren't the client's fault. */
public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
