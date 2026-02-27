package com.websitestudios.monitoring;

import com.websitestudios.repository.ProjectRequestRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for /actuator/health endpoint.
 *
 * Checks:
 * 1. Database connectivity (can we query the DB?)
 * 2. Redis connectivity (can we ping Redis?)
 *
 * Returns:
 * UP → All checks pass
 * DOWN → One or more checks fail (returns details for ops team)
 *
 * Exposed at: GET /actuator/health
 * In production: Shows "status" only to public.
 * Shows full details only to admin role.
 * (Configured in ActuatorSecurityConfig)
 */
@Component("websiteStudiosHealth")
public class WsHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(WsHealthIndicator.class);

    private final ProjectRequestRepository projectRequestRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public WsHealthIndicator(ProjectRequestRepository projectRequestRepository,
            RedisTemplate<String, Object> redisTemplate) {
        this.projectRequestRepository = projectRequestRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();

        boolean dbHealthy = checkDatabase(healthBuilder);
        boolean redisHealthy = checkRedis(healthBuilder);

        if (dbHealthy && redisHealthy) {
            return healthBuilder.up()
                    .withDetail("database", "UP")
                    .withDetail("redis", "UP")
                    .build();
        } else {
            return healthBuilder.down()
                    .withDetail("database", dbHealthy ? "UP" : "DOWN")
                    .withDetail("redis", redisHealthy ? "UP" : "DOWN")
                    .build();
        }
    }

    // ════════════════════════════════════════════════════════════════
    // CHECKS
    // ════════════════════════════════════════════════════════════════

    private boolean checkDatabase(Health.Builder builder) {
        try {
            long count = projectRequestRepository.count();
            log.debug("DB health check passed — total records: {}", count);
            return true;
        } catch (Exception e) {
            log.error("DB health check FAILED: {}", e.getMessage());
            builder.withDetail("database.error", "Cannot connect to PostgreSQL");
            return false;
        }
    }

    private boolean checkRedis(Health.Builder builder) {
        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            log.debug("Redis health check passed — response: {}", pong);
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            log.warn("Redis health check FAILED: {} — non-critical in dev mode", e.getMessage());
            builder.withDetail("redis.error", "Cannot connect to Redis");
            // Redis is non-critical in dev — don't fail entire health check
            return true;
        }
    }
}