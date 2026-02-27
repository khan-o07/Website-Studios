package com.websitestudios.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO for returning audit trail entries to SUPER_ADMIN.
 * Safe subset of AuditTrail entity — no internal DB details exposed.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditTrailResponseDTO {

    private Long id;
    private String adminUsername;
    private String action;
    private String targetEntity;
    private Long targetId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private LocalDateTime performedAt;

    // ──────────────────────────── Constructors ────────────────────────────

    public AuditTrailResponseDTO() {
    }

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }
}