package com.websitestudios.exception;

/**
 * Thrown when authentication fails or is missing.
 * Maps to HTTP 401 Unauthorized.
 *
 * Will be actively used in Phase 6 (JWT Auth).
 *
 * Usage:
 * throw new UnauthorizedException("Invalid or expired JWT token");
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}