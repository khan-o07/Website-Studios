package com.websitestudios.controller.v1;

import com.websitestudios.dto.response.WsApiResponseDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public health check endpoint.
 * Used by: Kubernetes liveness/readiness probes, uptime monitors, load
 * balancers.
 *
 * UPDATED in Phase 9:
 * - Returns build version and environment
 * - Separate /ping endpoint for lightweight liveness checks
 * - Full health details at /api/v1/health (public-safe subset)
 * - Full Actuator health at /actuator/health (ops team)
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {

    @Value("${spring.application.name:website-studios-api}")
    private String appName;

    @Value("${ws.app.version:1.0.0}")
    private String appVersion;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * GET /api/v1/health
     * Full health response — safe public fields only.
     */
    @GetMapping
    public ResponseEntity<WsApiResponseDTO<Map<String, Object>>> health() {
        Map<String, Object> healthData = new LinkedHashMap<>();
        healthData.put("status", "UP");
        healthData.put("application", appName);
        healthData.put("version", appVersion);
        healthData.put("profile", activeProfile);
        healthData.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Service is healthy", healthData));
    }

    /**
     * GET /api/v1/health/ping
     * Ultra-lightweight liveness check (< 1ms).
     * Used by Kubernetes liveness probes.
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    /**
     * GET /api/v1/health/ready
     * Readiness probe — indicates if the service can accept traffic.
     * Used by Kubernetes readiness probes.
     */
    @GetMapping("/ready")
    public ResponseEntity<WsApiResponseDTO<Map<String, String>>> ready() {
        Map<String, String> readiness = new LinkedHashMap<>();
        readiness.put("status", "READY");
        readiness.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Service is ready to accept traffic", readiness));
    }
}