package com.dsanext.exception;

/**
 * Thrown for domain-level validation failures not caught by @Valid.
 * Maps to HTTP 400 Bad Request.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
