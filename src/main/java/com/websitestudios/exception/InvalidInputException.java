package com.websitestudios.exception;

/**
 * Thrown when input fails business-level validation
 * (beyond what JSR-303 annotations catch).
 * Maps to HTTP 400 Bad Request.
 *
 * Usage:
 * throw new InvalidInputException("Invalid status value: UNKNOWN");
 */
public class InvalidInputException extends RuntimeException {

    private final String fieldName;

    public InvalidInputException(String message) {
        super(message);
        this.fieldName = null;
    }

    public InvalidInputException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}