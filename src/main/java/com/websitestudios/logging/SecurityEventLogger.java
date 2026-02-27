package com.websitestudios.logging;

import com.websitestudios.enums.AuditActionEnum;
import com.websitestudios.service.AuditTrailService;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Security Event Logger — logs suspicious and notable security events.
 *
 * This is a thin facade over AuditTrailService that:
 * 1. Writes to the structured application log (SLF4J)
 * 2. Persists to the audit_trail DB table (async)
 *
 * Called from:
 * - RateLimitFilter (rate limit exceeded)
 * - RecaptchaService (low score detected)
 * - JwtAuthenticationFilter (invalid tokens)
 * - AccountLockoutService (account locked)
 * - AuthenticationController (login events)
 */
@Component
public class SecurityEventLogger {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventLogger.class);

    private final AuditTrailService auditTrailService;

    public SecurityEventLogger(AuditTrailService auditTrailService) {
        this.auditTrailService = auditTrailService;
    }

    // ════════════════════════════════════════════════════════════════
    // SECURITY EVENT METHODS
    // ════════════════════════════════════════════════════════════════

    /**
     * Log a rate limit exceeded event.
     */
    public void logRateLimitExceeded(String ipAddress, String path, String method) {
        log.warn("[SECURITY] RATE_LIMIT_EXCEEDED | IP: {} | {}: {}",
                ipAddress, method, path);

        auditTrailService.logSecurityEvent(
                AuditActionEnum.RATE_LIMIT_EXCEEDED,
                "Rate limit exceeded on " + method + " " + path,
                ipAddress,
                null);
    }

    /**
     * Log a suspicious reCAPTCHA score.
     */
    public void logSuspiciousCaptcha(String ipAddress, double score) {
        log.warn("[SECURITY] SUSPICIOUS_CAPTCHA | IP: {} | Score: {}", ipAddress, score);

        auditTrailService.logSecurityEvent(
                AuditActionEnum.SUSPICIOUS_CAPTCHA,
                "reCAPTCHA score too low: " + score,
                ipAddress,
                null);
    }

    /**
     * Log an invalid JWT token attempt.
     */
    public void logInvalidTokenAttempt(String ipAddress, String path, String reason) {
        log.warn("[SECURITY] INVALID_TOKEN_ATTEMPT | IP: {} | Path: {} | Reason: {}",
                ipAddress, path, reason);

        auditTrailService.logSecurityEvent(
                AuditActionEnum.INVALID_TOKEN_ATTEMPT,
                "Invalid token on " + path + ": " + reason,
                ipAddress,
                null);
    }

    /**
     * Log an account lockout event.
     */
    public void logAccountLocked(String username, String ipAddress, int failedAttempts) {
        log.warn("[SECURITY] ACCOUNT_LOCKED | User: {} | IP: {} | Attempts: {}",
                username, ipAddress, failedAttempts);

        auditTrailService.logSecurityEvent(
                AuditActionEnum.ACCOUNT_LOCKED,
                "Account locked after " + failedAttempts + " failed attempts: " + username,
                ipAddress,
                null);
    }

    /**
     * Log a login success event.
     */
    public void logLoginSuccess(String username, String ipAddress, String userAgent) {
        log.info("[SECURITY] LOGIN_SUCCESS | User: {} | IP: {}", username, ipAddress);

        auditTrailService.logLogin(username, true, ipAddress, userAgent, null);
    }

    /**
     * Log a login failure event.
     */
    public void logLoginFailure(String username, String ipAddress,
            String userAgent, String reason) {
        log.warn("[SECURITY] LOGIN_FAILURE | User: {} | IP: {} | Reason: {}",
                username, ipAddress, reason);

        auditTrailService.logLogin(username, false, ipAddress, userAgent, reason);
    }

    /**
     * Log from an HttpServletRequest (extracts IP automatically).
     */
    public void logRateLimitExceeded(HttpServletRequest request) {
        String ip = extractIp(request);
        logRateLimitExceeded(ip, request.getServletPath(), request.getMethod());
    }

    // ════════════════════════════════════════════════════════════════
    // UTILITY
    // ════════════════════════════════════════════════════════════════

    private String extractIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}