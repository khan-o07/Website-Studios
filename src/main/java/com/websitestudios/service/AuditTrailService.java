package com.websitestudios.service;

import com.websitestudios.enums.AuditActionEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.websitestudios.entity.AuditTrail;

import java.time.LocalDateTime;

/**
 * Service interface for audit trail operations.
 */
public interface AuditTrailService {

    /**
     * Log a generic admin action.
     */
    void log(String adminUsername, AuditActionEnum action,
            String targetEntity, Long targetId,
            String oldValue, String newValue,
            String ipAddress, String userAgent);

    /**
     * Log a status change on a project request.
     */
    void logStatusChange(String adminUsername, Long requestId,
            String oldStatus, String newStatus,
            String ipAddress, String userAgent);

    /**
     * Log a soft delete action.
     */
    void logDelete(String adminUsername, Long requestId,
            String ipAddress, String userAgent);

    /**
     * Log a data view action (admin viewed a request).
     */
    void logView(String adminUsername, Long requestId,
            String ipAddress, String userAgent);

    /**
     * Log a login event (success or failure).
     */
    void logLogin(String username, boolean success,
            String ipAddress, String userAgent, String failureReason);

    /**
     * Log a security event (suspicious activity, lockout, etc).
     */
    void logSecurityEvent(AuditActionEnum action, String details,
            String ipAddress, String userAgent);

    /**
     * Log a data export.
     */
    void logExport(String adminUsername, String exportType,
            String ipAddress, String userAgent);

    // ── Query methods ──────────────────────────────────────────────

    Page<AuditTrail> getAllAuditLogs(Pageable pageable);

    Page<AuditTrail> getAuditLogsByAdmin(String username, Pageable pageable);

    Page<AuditTrail> getAuditLogsByAction(AuditActionEnum action, Pageable pageable);

    Page<AuditTrail> getAuditLogsForEntity(String entity, Long entityId, Pageable pageable);

    Page<AuditTrail> getAuditLogsByTimeRange(LocalDateTime start,
            LocalDateTime end, Pageable pageable);
}