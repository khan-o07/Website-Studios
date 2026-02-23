package com.websitestudios.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper for ALL endpoints.
 *
 * Success example:
 * {
 * "success": true,
 * "message": "Project request submitted successfully",
 * "data": { ... },
 * "timestamp": "2025-01-15T10:30:00"
 * }
 *
 * Error example:
 * {
 * "success": false,
 * "message": "Validation failed",
 * "data": null,
 * "timestamp": "2025-01-15T10:30:00"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsApiResponseDTO<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // ──────────────────────────── Constructors ────────────────────────────

    public WsApiResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public WsApiResponseDTO(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // ──────────────────────────── Factory Methods ────────────────────────────

    /**
     * Create a success response with data.
     */
    public static <T> WsApiResponseDTO<T> success(String message, T data) {
        return new WsApiResponseDTO<>(true, message, data);
    }

    /**
     * Create a success response without data.
     */
    public static <T> WsApiResponseDTO<T> success(String message) {
        return new WsApiResponseDTO<>(true, message, null);
    }

    /**
     * Create an error response.
     */
    public static <T> WsApiResponseDTO<T> error(String message) {
        return new WsApiResponseDTO<>(false, message, null);
    }

    /**
     * Create an error response with detail data (e.g., validation errors map).
     */
    public static <T> WsApiResponseDTO<T> error(String message, T data) {
        return new WsApiResponseDTO<>(false, message, data);
    }

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}