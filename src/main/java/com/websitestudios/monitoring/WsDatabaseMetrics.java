package com.websitestudios.monitoring;

import com.websitestudios.repository.ProjectRequestRepository;
import com.websitestudios.enums.ProjectStatusEnum;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled metrics collector — updates Prometheus gauges from the database
 * on a fixed schedule.
 *
 * Why scheduled instead of real-time?
 * - DB queries are expensive — don't run on every request
 * - Prometheus scrapes every 15s — updating every 30s is fine
 * - Uses @Scheduled (lightweight, no extra thread pool needed)
 *
 * Metrics produced:
 * ws.db.requests.by.status{status="PENDING"} → Count of PENDING requests
 * ws.db.requests.by.status{status="IN_PROGRESS"} → Count of IN_PROGRESS
 * ws.db.requests.by.status{status="COMPLETED"} → Count of COMPLETED
 * ws.db.requests.by.status{status="CANCELLED"} → Count of CANCELLED
 */
@Component
public class WsDatabaseMetrics {

    private static final Logger log = LoggerFactory.getLogger(WsDatabaseMetrics.class);

    private final ProjectRequestRepository projectRequestRepository;
    private final WsMetricsService wsMetricsService;
    private final MultiGauge requestsByStatus;

    public WsDatabaseMetrics(ProjectRequestRepository projectRequestRepository,
            WsMetricsService wsMetricsService,
            MeterRegistry meterRegistry) {
        this.projectRequestRepository = projectRequestRepository;
        this.wsMetricsService = wsMetricsService;

        this.requestsByStatus = MultiGauge.builder("ws.db.requests.by.status")
                .description("Active project requests grouped by status")
                .register(meterRegistry);
    }

    /**
     * Refresh metrics every 30 seconds.
     * Aligned with Prometheus default scrape interval.
     */
    @Scheduled(fixedDelayString = "${ws.metrics.refresh-interval-ms:30000}")
    public void refreshMetrics() {
        try {
            log.debug("Refreshing database metrics");

            long total = projectRequestRepository.countByIsDeletedFalse();
            long pending = projectRequestRepository
                    .countByStatusAndIsDeletedFalse(ProjectStatusEnum.PENDING);
            long inProgress = projectRequestRepository
                    .countByStatusAndIsDeletedFalse(ProjectStatusEnum.IN_PROGRESS);
            long completed = projectRequestRepository
                    .countByStatusAndIsDeletedFalse(ProjectStatusEnum.COMPLETED);
            long cancelled = projectRequestRepository
                    .countByStatusAndIsDeletedFalse(ProjectStatusEnum.CANCELLED);

            // Update gauges
            wsMetricsService.setTotalRequests(total);
            wsMetricsService.setPendingRequests(pending);

            // Update multi-gauge (one entry per status)
            requestsByStatus.register(
                    List.of(
                            MultiGauge.Row.of(Tags.of("status", "PENDING"), pending),
                            MultiGauge.Row.of(Tags.of("status", "IN_PROGRESS"), inProgress),
                            MultiGauge.Row.of(Tags.of("status", "COMPLETED"), completed),
                            MultiGauge.Row.of(Tags.of("status", "CANCELLED"), cancelled)),
                    true // Override existing rows
            );

            log.debug("Metrics refreshed — total: {}, pending: {}, in_progress: {}, " +
                    "completed: {}, cancelled: {}",
                    total, pending, inProgress, completed, cancelled);

        } catch (Exception e) {
            log.error("Failed to refresh database metrics: {}", e.getMessage());
            // Never throw — metrics failure must not impact the application
        }
    }
}