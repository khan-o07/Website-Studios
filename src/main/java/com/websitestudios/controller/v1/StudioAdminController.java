package com.websitestudios.controller.v1;

import com.websitestudios.dto.response.WsApiResponseDTO;
import com.websitestudios.service.ProjectRequestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin-only operations for studio management.
 *
 * ALL endpoints require authentication + appropriate role.
 *
 * TODO Phase 6: @PreAuthorize("hasRole('ADMIN')") on class level
 * TODO Phase 8: Audit trail logging, export functionality
 *
 * Endpoints:
 * GET /api/v1/admin/dashboard → ADMIN → Dashboard stats
 * GET /api/v1/admin/audit-trail → SUPER_ADMIN → Audit logs (Phase 8)
 * GET /api/v1/admin/export → SUPER_ADMIN → Data export (Phase 8)
 */
@RestController
@RequestMapping("/api/v1/admin")
public class StudioAdminController {

    private static final Logger log = LoggerFactory.getLogger(StudioAdminController.class);

    private final ProjectRequestService projectRequestService;

    public StudioAdminController(ProjectRequestService projectRequestService) {
        this.projectRequestService = projectRequestService;
    }

    // ════════════════════════════════════════════════════════════════
    // ADMIN: Dashboard Statistics
    // ════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/admin/dashboard
     *
     * Returns basic statistics for the admin dashboard.
     *
     * Response:
     * {
     * "success": true,
     * "message": "Dashboard data retrieved successfully",
     * "data": {
     * "totalRequests": 150,
     * "pendingRequests": 45,
     * "inProgressRequests": 30,
     * "completedRequests": 60,
     * "cancelledRequests": 15
     * }
     * }
     *
     * TODO Phase 6: @PreAuthorize("hasRole('ADMIN')")
     */
    @GetMapping("/dashboard")
    public ResponseEntity<WsApiResponseDTO<Map<String, Long>>> getDashboardStats() {

        log.info("Admin fetching dashboard statistics");

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalRequests", projectRequestService.getTotalCount());
        stats.put("pendingRequests", projectRequestService.getCountByStatus("PENDING"));
        stats.put("inProgressRequests", projectRequestService.getCountByStatus("IN_PROGRESS"));
        stats.put("completedRequests", projectRequestService.getCountByStatus("COMPLETED"));
        stats.put("cancelledRequests", projectRequestService.getCountByStatus("CANCELLED"));

        log.info("Dashboard stats: {}", stats);

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Dashboard data retrieved successfully", stats));
    }

    // ════════════════════════════════════════════════════════════════
    // SUPER_ADMIN: Audit Trail (Placeholder for Phase 8)
    // ════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/admin/audit-trail
     *
     * Returns admin action audit logs.
     * Will be fully implemented in Phase 8.
     *
     * TODO Phase 6: @PreAuthorize("hasRole('SUPER_ADMIN')")
     * TODO Phase 8: AuditTrailService integration
     */
    @GetMapping("/audit-trail")
    public ResponseEntity<WsApiResponseDTO<String>> getAuditTrail() {

        log.info("Super Admin requested audit trail");

        // Placeholder — will be implemented in Phase 8
        return ResponseEntity.ok(
                WsApiResponseDTO.success("Audit trail will be available in Phase 8"));
    }

    // ════════════════════════════════════════════════════════════════
    // SUPER_ADMIN: Data Export (Placeholder for Phase 8)
    // ════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/admin/export
     *
     * Exports project request data.
     * Will be fully implemented in Phase 8.
     *
     * TODO Phase 6: @PreAuthorize("hasRole('SUPER_ADMIN')")
     * TODO Phase 8: CSV/JSON export implementation
     */
    @GetMapping("/export")
    public ResponseEntity<WsApiResponseDTO<String>> exportData() {

        log.info("Super Admin requested data export");

        // Placeholder — will be implemented in Phase 8
        return ResponseEntity.ok(
                WsApiResponseDTO.success("Data export will be available in Phase 8"));
    }
}