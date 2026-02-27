package com.websitestudios.config;

import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Prometheus and Micrometer metric configuration.
 *
 * Configures:
 * - SLA boundaries for request timers (latency histogram buckets)
 * - Percentile calculations (p50, p95, p99)
 * - Metric filtering (exclude noisy/unnecessary metrics)
 * - Common tags applied to ALL metrics
 *
 * Metric naming convention:
 * Spring Boot auto-metrics use snake_case (e.g., http_server_requests_seconds)
 * Custom ws metrics use dots (e.g., ws.requests.form.submitted)
 * Prometheus converts both to underscore format automatically.
 */
@Configuration
public class PrometheusConfig {

    private static final Logger log = LoggerFactory.getLogger(PrometheusConfig.class);

    /**
     * Configure histogram buckets and percentile settings for all timers.
     *
     * SLA boundaries define the histogram bucket boundaries exposed to Prometheus.
     * These map to Grafana's response time distribution panels.
     */
    @Bean
    public MeterFilter prometheusTimerConfig() {
        log.info("Configuring Prometheus metric distribution settings");

        return new MeterFilter() {
            @Override
            @SuppressWarnings("deprecation")
            public DistributionStatisticConfig configure(
                    io.micrometer.core.instrument.Meter.Id id,
                    DistributionStatisticConfig config) {

                // Apply histogram config to all timers
                if (id.getType() == io.micrometer.core.instrument.Meter.Type.TIMER) {
                    return DistributionStatisticConfig.builder()
                            .percentilesHistogram(true)
                            .percentiles(0.50, 0.75, 0.95, 0.99)
                            .serviceLevelObjectives(
                                    Duration.ofMillis(50).toNanos(),
                                    Duration.ofMillis(100).toNanos(),
                                    Duration.ofMillis(500).toNanos(),
                                    Duration.ofSeconds(1).toNanos(),
                                    Duration.ofSeconds(2).toNanos(),
                                    Duration.ofSeconds(5).toNanos())
                            .minimumExpectedValue(Duration.ofMillis(1).toNanos())
                            .maximumExpectedValue(Duration.ofSeconds(30).toNanos())
                            .expiry(Duration.ofMinutes(5))
                            .build()
                            .merge(config);
                }

                return config;
            }
        };
    }

    /**
     * Filter out high-cardinality or noisy metrics from Prometheus.
     * High cardinality metrics can cause Prometheus to run out of memory.
     */
    @Bean
    public MeterFilter denyHighCardinalityMetrics() {
        return MeterFilter.denyNameStartsWith("jvm.gc.pause");
    }
}