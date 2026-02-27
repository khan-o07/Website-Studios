package com.websitestudios.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async executor configuration for audit logging.
 *
 * Audit logging runs in a dedicated thread pool:
 * - Never blocks the main HTTP request thread
 * - Isolated from application thread pool
 * - Bounded queue prevents memory exhaustion under load
 *
 * Thread pool sizing:
 * Core: 2 threads (always alive, handle normal load)
 * Max: 5 threads (burst capacity)
 * Queue: 100 tasks (buffer before rejecting)
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Dedicated thread pool for audit trail logging.
     * Named "auditExecutor" — matches @Async("auditExecutor") annotations.
     */
    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        log.info("Creating audit thread pool executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("audit-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // When queue is full — run in caller's thread (don't drop audit logs)
        executor.setRejectedExecutionHandler(
                (r, exec) -> {
                    log.warn("Audit executor queue full — running audit log in caller thread");
                    r.run();
                });

        executor.initialize();
        return executor;
    }
}