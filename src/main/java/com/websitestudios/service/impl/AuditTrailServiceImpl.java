package com.websitestudios.service.impl;

import com.websitestudios.entity.AuditTrail;
import com.websitestudios.entity.StudioAdmin;
import com.websitestudios.enums.AuditActionEnum;
import com.websitestudios.repository.AuditTrailRepository;
import com.websitestudios.repository.StudioAdminRepository;
import com.websitestudios.service.AuditTrailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * AuditTrailService implementation.
 *
 * Design principles:
 * - All logging is ASYNC (@Async) — never blocks the main request thread
 * - Uses REQUIRES_NEW propagation — audit log saves even if main tx fails
 * - Never throws exceptions — audit failure must not break the main flow
 * - Sensitive data is NEVER stored in old/new values (only status names, IDs)
 */
@Service
public class AuditTrailServiceImpl implements AuditTrailService {

    private static final Logger log = LoggerFactory.getLogger(AuditTrailServiceImpl.class);

    private final AuditTrailRepository auditTrailRepository;
    private final StudioAdminRepository studioAdminRepository;

    public AuditTrailServiceImpl(AuditTrailRepository auditTrailRepository,
            StudioAdminRepository studioAdminRepository) {
        this.auditTrailRepository = auditTrailRepository;
        this.studioAdminRepository = studioAdminRepository;
    }

    // ════════════════════════════════════════════════════════════════
    // CORE LOGGING METHOD
    // ════════════════════════════════════════════════════════════════

    /**
     * Core logging method — all other log methods delegate here.
     *
     * @Async → runs in a separate thread pool (never blocks request)
     *        REQUIRES_NEW → independent transaction (survives main tx rollback)
     */
    @Override
    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String adminUsername, AuditActionEnum action,
            String targetEntity, Long targetId,
            String oldValue, String newValue,
            String ipAddress, String userAgent) {
        try {
            AuditTrail auditEntry = new AuditTrail();
            auditEntry.setAction(action);
            auditEntry.setTargetEntity(targetEntity);
            auditEntry.setTargetId(targetId);
            auditEntry.setOldValue(oldValue);
            auditEntry.setNewValue(newValue);
            auditEntry.setIpAddress(ipAddress != null ? ipAddress : "unknown");
            auditEntry.setUserAgent(truncate(userAgent, 500));
            auditEntry.setPerformedAt(LocalDateTime.now());

            // Resolve admin user (nullable for system/anonymous actions)
            if (adminUsername != null && !adminUsername.isBlank()
                    && !"anonymous".equalsIgnoreCase(adminUsername)) {
                Optional<StudioAdmin> admin = studioAdminRepository.findByUsername(adminUsername);
                admin.ifPresent(auditEntry::setAdminUser);
            }

            auditTrailRepository.save(auditEntry);

            log.debug("Audit logged: {} by {} on {}:{} from IP {}",
                    action, adminUsername, targetEntity, targetId, ipAddress);

        } catch (Exception e) {
            // NEVER propagate audit exceptions — log and continue
            log.error("AUDIT LOG FAILED — action: {}, admin: {}, error: {}",
                    action, adminUsername, e.getMessage(), e);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // CONVENIENCE LOGGING METHODS
    // ════════════════════════════════════════════════════════════════

    @Override
    @Async("auditExecutor")
    public void logStatusChange(String adminUsername, Long requestId,
            String oldStatus, String newStatus,
            String ipAddress, String userAgent) {
        log.info("AUDIT: {} changed ProjectRequest#{} status: {} → {} from IP: {}",
                adminUsername, requestId, oldStatus, newStatus, ipAddress);

        log(adminUsername, AuditActionEnum.STATUS_CHANGE,
                "ProjectRequest", requestId,
                oldStatus, newStatus,
                ipAddress, userAgent);
    }

    @Override
    @Async("auditExecutor")
    public void logDelete(String adminUsername, Long requestId,
            String ipAddress, String userAgent) {
        log.info("AUDIT: {} soft-deleted ProjectRequest#{} from IP: {}",
                adminUsername, requestId, ipAddress);

        log(adminUsername, AuditActionEnum.SOFT_DELETE,
                "ProjectRequest", requestId,
                "ACTIVE", "DELETED",
                ipAddress, userAgent);
    }

    @Override
    @Async("auditExecutor")
    public void logView(String adminUsername, Long requestId,
            String ipAddress, String userAgent) {
        log.debug("AUDIT: {} viewed ProjectRequest#{} from IP: {}",
                adminUsername, requestId, ipAddress);

        log(adminUsername, AuditActionEnum.VIEW_REQUEST,
                "ProjectRequest", requestId,
                null, null,
                ipAddress, userAgent);
    }

    @Override
    @Async("auditExecutor")
    public void logLogin(String username, boolean success,
            String ipAddress, String userAgent, String failureReason) {
        AuditActionEnum action = success
                ? AuditActionEnum.LOGIN_SUCCESS
                : AuditActionEnum.LOGIN_FAILURE;

        if (success) {
            log.info("AUDIT: Successful login for user: {} from IP: {}", username, ipAddress);
        } else {
            log.warn("AUDIT: Failed login for user: {} from IP: {} — reason: {}",
                    username, ipAddress, failureReason);
        }

        log(username, action,
                "StudioAdmin", null,
                null, failureReason,
                ipAddress, userAgent);
    }

    @Override
    @Async("auditExecutor")
    public void logSecurityEvent(AuditActionEnum action, String details,
            String ipAddress, String userAgent) {
        log.warn("AUDIT SECURITY EVENT: {} from IP: {} — details: {}",
                action, ipAddress, details);

        log(null, action,
                "SECURITY", null,
                null, truncate(details, 500),
                ipAddress, userAgent);
    }

    @Override
    @Async("auditExecutor")
    public void logExport(String adminUsername, String exportType,
            String ipAddress, String userAgent) {
        log.info("AUDIT: {} exported data ({}) from IP: {}",
                adminUsername, exportType, ipAddress);

        log(adminUsername, AuditActionEnum.EXPORT_DATA,
                "ProjectRequest", null,
                null, exportType,
                ipAddress, userAgent);
    }

    // ════════════════════════════════════════════════════════════════
    // QUERY METHODS
    // ════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Page<AuditTrail> getAllAuditLogs(Pageable pageable) {
        return auditTrailRepository.findAllByOrderByPerformedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditTrail> getAuditLogsByAdmin(String username, Pageable pageable) {
        return auditTrailRepository
                .findByAdminUser_UsernameOrderByPerformedAtDesc(username, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditTrail> getAuditLogsByAction(AuditActionEnum action, Pageable pageable) {
        return auditTrailRepository.findByActionOrderByPerformedAtDesc(action, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditTrail> getAuditLogsForEntity(String entity, Long entityId, Pageable pageable) {
        return auditTrailRepository
                .findByTargetEntityAndTargetIdOrderByPerformedAtDesc(entity, entityId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditTrail> getAuditLogsByTimeRange(LocalDateTime start,
            LocalDateTime end, Pageable pageable) {
        return auditTrailRepository.findByTimeRange(start, end, pageable);
    }

    // ════════════════════════════════════════════════════════════════
    // UTILITY
    // ════════════════════════════════════════════════════════════════

    private String truncate(String value, int maxLength) {
        if (value == null)
            return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}