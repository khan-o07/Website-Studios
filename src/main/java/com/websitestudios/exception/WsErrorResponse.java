package com.websitestudios.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response structure for ALL error scenarios.
 *
 * SECURITY: Never exposes:
 * - Stack traces
 * - Class names
 * - SQL details
 * - Internal server paths
 * - Library versions
 *
 * Example (validation error):
 * {
 * "status": 400,
 * "error": "Validation Failed",
 * "message": "One or more fields have invalid values",
 * "fieldErrors": {
 * "fullName": "Full name is required",
 * "email": "Invalid email format"
 * },
 * "timestamp": "2025-01-15T10:30:00"
 * }
 *
 * Example (generic error):
 * {
 * "status": 404,
 * "error": "Not Found",
 * "message": "Project request not found with ID: 42",
 * "timestamp": "2025-01-15T10:30:00"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsErrorResponse {

    private int status;
    private String error;
    private String message;
    private Map<String, String> fieldErrors;
    private String path;
    private LocalDateTime timestamp;

    // ──────────────────────────── Constructors ────────────────────────────

    public WsErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public WsErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public WsErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    // ──────────────────────────── Factory Methods ────────────────────────────

    public static WsErrorResponse of(int status, String error, String message) {
        return new WsErrorResponse(status, error, message);
    }

    public static WsErrorResponse validationError(String message, Map<String, String> fieldErrors) {
        WsErrorResponse response = new WsErrorResponse(400, "Validation Failed", message);
        response.setFieldErrors(fieldErrors);
        return response;
    }

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}