package com.dsanext.exception;

/**
 * Thrown when authentication is required or a JWT is invalid/expired.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
