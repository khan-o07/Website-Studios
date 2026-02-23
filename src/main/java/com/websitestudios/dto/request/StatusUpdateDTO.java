package com.websitestudios.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for admin status update on a project request.
 * Used with PUT /api/v1/project-requests/{id}/status
 */
public class StatusUpdateDTO {

    @NotBlank(message = "Status is required")
    // TODO Phase 4: Add custom validator to ensure value exists in
    // ProjectStatusEnum
    private String status;

    // ──────────────────────────── Constructors ────────────────────────────

    public StatusUpdateDTO() {
    }

    public StatusUpdateDTO(String status) {
        this.status = status;
    }

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "StatusUpdateDTO{status='" + status + "'}";
    }
}