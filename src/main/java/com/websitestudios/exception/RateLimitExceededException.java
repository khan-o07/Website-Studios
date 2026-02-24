package com.websitestudios.exception;

/**
 * Thrown when a client exceeds the rate limit.
 * Maps to HTTP 429 Too Many Requests.
 *
 * Will be actively used in Phase 7 (Rate Limiting).
 *
 * Usage:
 * throw new RateLimitExceededException("Too many requests. Please try again in
 * 60 seconds.");
 */
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(String message) {
        super(message);
        this.retryAfterSeconds = 60;
    }

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}