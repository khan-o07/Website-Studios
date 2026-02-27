package com.websitestudios.entity;

import com.websitestudios.enums.AuditActionEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Audit trail entity — immutable record of every admin action.
 *
 * Design decisions:
 * - NO update/delete operations allowed on this table (append-only)
 * - DB user has INSERT-only permission on this table
 * - Stores old and new values for change tracking
 * - Records IP address and user agent for forensic analysis
 */
@Entity
@Table(name = "audit_trail")
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The admin who performed the action (nullable for system actions)
    @ManyToOne
    @JoinColumn(name = "admin_user_id", nullable = true)
    private StudioAdmin adminUser;

    // What action was performed
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditActionEnum action;

    // Which entity type was affected (e.g., "ProjectRequest", "StudioAdmin")
    @Column(name = "target_entity", nullable = false, length = 50)
    private String targetEntity;

    // ID of the affected entity
    @Column(name = "target_id", nullable = true)
    private Long targetId;

    // Value before the change (JSON or plain text)
    @Column(name = "old_value", columnDefinition = "TEXT", nullable = true)
    private String oldValue;

    // Value after the change (JSON or plain text)
    @Column(name = "new_value", columnDefinition = "TEXT", nullable = true)
    private String newValue;

    // Client IP at time of action
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    // Browser/client user agent
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // Additional context (JSON blob for flexible metadata)
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    // ──────────────────────────── Constructors ────────────────────────────

    public AuditTrail() {
        this.performedAt = LocalDateTime.now();
    }

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudioAdmin getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(StudioAdmin adminUser) {
        this.adminUser = adminUser;
    }

    public AuditActionEnum getAction() {
        return action;
    }

    public void setAction(AuditActionEnum action) {
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

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }
}