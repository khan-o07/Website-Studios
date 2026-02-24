package com.websitestudios.exception;

/**
 * Thrown when a duplicate project request is detected.
 * Duplicate = same email_hash AND phone_hash already exist (non-deleted).
 * Maps to HTTP 409 Conflict.
 *
 * Usage:
 * throw new DuplicateRequestException("A request with this email and phone
 * already exists");
 */
public class DuplicateRequestException extends RuntimeException {

    public DuplicateRequestException(String message) {
        super(message);
    }
}