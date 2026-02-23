package com.websitestudios.controller;

import com.websitestudios.dto.response.WsApiResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint for monitoring and load balancers.
 * Always PUBLIC — no authentication required.
 *
 * GET /api/v1/health
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<WsApiResponseDTO<Map<String, Object>>> healthCheck() {

        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "Website Studios API");
        healthData.put("timestamp", LocalDateTime.now().toString());

        // Intentionally minimal — never expose internal details
        // No DB status, no version numbers, no environment info

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Service is healthy", healthData));
    }
}