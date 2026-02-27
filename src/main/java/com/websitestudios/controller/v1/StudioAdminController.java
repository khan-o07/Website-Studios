package com.websitestudios.controller.v1;

import com.websitestudios.dto.response.AuditTrailResponseDTO;
import com.websitestudios.dto.response.PaginatedResponseDTO;
import com.websitestudios.dto.response.WsApiResponseDTO;
import com.websitestudios.enums.AuditActionEnum;
import com.websitestudios.entity.AuditTrail;
import com.websitestudios.mapper.AuditTrailMapper;
import com.websitestudios.service.AuditTrailService;
import com.websitestudios.service.ProjectRequestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * StudioAdminController — UPDATED in Phase 8 with full audit trail endpoint.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class StudioAdminController {

    private static final Logger log = LoggerFactory.getLogger(StudioAdminController.class);

    private final ProjectRequestService projectRequestService;
    private final AuditTrailService auditTrailService;
    private final AuditTrailMapper auditTrailMapper;

    public StudioAdminController(ProjectRequestService projectRequestService,
            AuditTrailService auditTrailService,
            AuditTrailMapper auditTrailMapper) {
        this.projectRequestService = projectRequestService;
        this.auditTrailService = auditTrailService;
        this.auditTrailMapper = auditTrailMapper;
    }

    // ════════════════════════════════════════════════════════════════
    // DASHBOARD
    // ════════════════════════════════════════════════════════════════

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<WsApiResponseDTO<Map<String, Long>>> getDashboardStats() {
        log.info("Admin fetching dashboard statistics");

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalRequests", projectRequestService.getTotalCount());
        stats.put("pendingRequests", projectRequestService.getCountByStatus("PENDING"));
        stats.put("inProgressRequests", projectRequestService.getCountByStatus("IN_PROGRESS"));
        stats.put("completedRequests", projectRequestService.getCountByStatus("COMPLETED"));
        stats.put("cancelledRequests", projectRequestService.getCountByStatus("CANCELLED"));

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Dashboard data retrieved successfully", stats));
    }

    // ════════════════════════════════════════════════════════════════
    // AUDIT TRAIL — FULLY IMPLEMENTED
    // ════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/admin/audit-trail
     *
     * Query params:
     * page → Page number (default: 0)
     * size → Page size (default: 50, max: 100)
     * action → Filter by AuditActionEnum value (optional)
     * admin → Filter by admin username (optional)
     *
     * Requires: SUPER_ADMIN role only.
     */
    @GetMapping("/audit-trail")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<WsApiResponseDTO<PaginatedResponseDTO<AuditTrailResponseDTO>>> getAuditTrail(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String admin,
            Authentication authentication,
            HttpServletRequest request) {

        log.info("Super Admin {} accessing audit trail — page: {}, size: {}, action: {}, admin: {}",
                authentication.getName(), page, size, action, admin);

        // Clamp size
        if (size > 100)
            size = 100;
        if (size < 1)
            size = 50;
        if (page < 0)
            page = 0;

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "performedAt"));

        Page<AuditTrail> auditPage;

        if (action != null && !action.isBlank()) {
            try {
                AuditActionEnum actionEnum = AuditActionEnum.valueOf(action.toUpperCase().trim());
                auditPage = auditTrailService.getAuditLogsByAction(actionEnum, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid audit action filter: {}", action);
                return ResponseEntity.badRequest()
                        .body(WsApiResponseDTO.error("Invalid action value: " + action));
            }
        } else if (admin != null && !admin.isBlank()) {
            auditPage = auditTrailService.getAuditLogsByAdmin(admin, pageable);
        } else {
            auditPage = auditTrailService.getAllAuditLogs(pageable);
        }

        Page<AuditTrailResponseDTO> dtoPage = auditPage.map(auditTrailMapper::toResponseDTO);

        // Log this access to the audit trail itself
        auditTrailService.log(
                authentication.getName(),
                AuditActionEnum.VIEW_AUDIT_TRAIL,
                "AuditTrail", null,
                null, null,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return ResponseEntity.ok(
                WsApiResponseDTO.success(
                        "Audit trail retrieved successfully",
                        PaginatedResponseDTO.from(dtoPage)));
    }

    // ════════════════════════════════════════════════════════════════
    // EXPORT — SUPER_ADMIN
    // ════════════════════════════════════════════════════════════════

    @GetMapping("/export")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<WsApiResponseDTO<String>> exportData(
            Authentication authentication,
            HttpServletRequest request) {

        log.info("Super Admin {} requested data export", authentication.getName());

        auditTrailService.logExport(
                authentication.getName(),
                "CSV_EXPORT",
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        // Full CSV export implementation can be added here
        return ResponseEntity.ok(
                WsApiResponseDTO.success(
                        "Export functionality is ready. Full CSV export implementation pending."));
    }
}