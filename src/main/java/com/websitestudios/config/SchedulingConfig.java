package com.websitestudios.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring scheduling for:
 * - WsDatabaseMetrics: refreshes Prometheus gauges every 30s
 *
 * Note: Scheduling is automatically disabled in tests via
 * 
 * @MockBean or by using the "test" profile.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);

    public SchedulingConfig() {
        log.info("Scheduling enabled — WsDatabaseMetrics will refresh every 30s");
    }
}
