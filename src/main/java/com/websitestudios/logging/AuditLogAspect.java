package com.websitestudios.logging;

import com.websitestudios.enums.AuditActionEnum;
import com.websitestudios.service.AuditTrailService;
import com.websitestudios.ratelimit.IpAddressUtil;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP Aspect — automatically logs admin actions without modifying controller
 * code.
 *
 * Intercepts calls to admin controller methods and logs them to the audit
 * trail.
 * This keeps audit logging CROSS-CUTTING and decoupled from business logic.
 *
 * Pointcuts:
 * - updateProjectRequestStatus → logs STATUS_CHANGE
 * - softDeleteProjectRequest → logs SOFT_DELETE
 * - getDashboardStats → logs VIEW_DASHBOARD
 * - getAllProjectRequests → logs VIEW_REQUEST (list)
 * - getProjectRequestById → logs VIEW_REQUEST (single)
 *
 * NOTE: Explicit logStatusChange() calls in the service are ALSO kept
 * for capturing old/new values which AOP can't easily access.
 * AOP handles the "who did what" logging; service handles "what changed".
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    private final AuditTrailService auditTrailService;
    private final IpAddressUtil ipAddressUtil;

    public AuditLogAspect(AuditTrailService auditTrailService,
            IpAddressUtil ipAddressUtil) {
        this.auditTrailService = auditTrailService;
        this.ipAddressUtil = ipAddressUtil;
    }

    // ════════════════════════════════════════════════════════════════
    // STATUS CHANGE
    // ════════════════════════════════════════════════════════════════

    @AfterReturning(pointcut = "execution(* com.websitestudios.controller.v1.ProjectRequestController" +
            ".updateProjectRequestStatus(..))", returning = "result")
    public void logStatusChange(JoinPoint joinPoint, Object result) {
        try {
            Object[] args = joinPoint.getArgs();
            Long requestId = args.length > 0 ? (Long) args[0] : null;

            auditTrailService.log(
                    getCurrentUsername(),
                    AuditActionEnum.STATUS_CHANGE,
                    "ProjectRequest", requestId,
                    null, null,
                    getCurrentIp(), getCurrentUserAgent());
        } catch (Exception e) {
            log.debug("AOP audit log failed for statusChange: {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // SOFT DELETE
    // ════════════════════════════════════════════════════════════════

    @AfterReturning(pointcut = "execution(* com.websitestudios.controller.v1.ProjectRequestController" +
            ".softDeleteProjectRequest(..))")
    public void logSoftDelete(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            Long requestId = args.length > 0 ? (Long) args[0] : null;

            auditTrailService.log(
                    getCurrentUsername(),
                    AuditActionEnum.SOFT_DELETE,
                    "ProjectRequest", requestId,
                    "ACTIVE", "DELETED",
                    getCurrentIp(), getCurrentUserAgent());
        } catch (Exception e) {
            log.debug("AOP audit log failed for softDelete: {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // VIEW SINGLE REQUEST
    // ════════════════════════════════════════════════════════════════

    @AfterReturning(pointcut = "execution(* com.websitestudios.controller.v1.ProjectRequestController" +
            ".getProjectRequestById(..))", returning = "result")
    public void logViewSingle(JoinPoint joinPoint, Object result) {
        try {
            Object[] args = joinPoint.getArgs();
            Long requestId = args.length > 0 ? (Long) args[0] : null;

            auditTrailService.log(
                    getCurrentUsername(),
                    AuditActionEnum.VIEW_REQUEST,
                    "ProjectRequest", requestId,
                    null, null,
                    getCurrentIp(), getCurrentUserAgent());
        } catch (Exception e) {
            log.debug("AOP audit log failed for viewSingle: {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // VIEW DASHBOARD
    // ════════════════════════════════════════════════════════════════

    @AfterReturning(pointcut = "execution(* com.websitestudios.controller.v1.StudioAdminController" +
            ".getDashboardStats(..))")
    public void logDashboardView(JoinPoint joinPoint) {
        try {
            auditTrailService.log(
                    getCurrentUsername(),
                    AuditActionEnum.VIEW_DASHBOARD,
                    "Dashboard", null,
                    null, null,
                    getCurrentIp(), getCurrentUserAgent());
        } catch (Exception e) {
            log.debug("AOP audit log failed for dashboardView: {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // EXPORT DATA
    // ════════════════════════════════════════════════════════════════

    @AfterReturning(pointcut = "execution(* com.websitestudios.controller.v1.StudioAdminController" +
            ".exportData(..))")
    public void logExport(JoinPoint joinPoint) {
        try {
            auditTrailService.log(
                    getCurrentUsername(),
                    AuditActionEnum.EXPORT_DATA,
                    "ProjectRequest", null,
                    null, "CSV_EXPORT",
                    getCurrentIp(), getCurrentUserAgent());
        } catch (Exception e) {
            log.debug("AOP audit log failed for export: {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // UTILITIES
    // ════════════════════════════════════════════════════════════════

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getName();
            }
        } catch (Exception e) {
            log.debug("Could not extract current username from SecurityContext");
        }
        return "anonymous";
    }

    private String getCurrentIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return ipAddressUtil.extractClientIp(attrs.getRequest());
            }
        } catch (Exception e) {
            log.debug("Could not extract IP from request context");
        }
        return "unknown";
    }

    private String getCurrentUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String ua = attrs.getRequest().getHeader("User-Agent");
                return ua != null && ua.length() > 500 ? ua.substring(0, 500) : ua;
            }
        } catch (Exception e) {
            log.debug("Could not extract User-Agent from request context");
        }
        return null;
    }
}