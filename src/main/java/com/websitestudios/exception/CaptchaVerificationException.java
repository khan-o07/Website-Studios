package com.websitestudios.exception;

/**
 * Thrown when reCAPTCHA verification fails.
 * Maps to HTTP 400 Bad Request.
 *
 * Will be actively used in Phase 7 (reCAPTCHA integration).
 *
 * Usage:
 * throw new CaptchaVerificationException("reCAPTCHA verification failed. Score:
 * 0.2");
 */
public class CaptchaVerificationException extends RuntimeException {

    private final Double score;

    public CaptchaVerificationException(String message) {
        super(message);
        this.score = null;
    }

    public CaptchaVerificationException(String message, Double score) {
        super(message);
        this.score = score;
    }

    public Double getScore() {
        return score;
    }
}