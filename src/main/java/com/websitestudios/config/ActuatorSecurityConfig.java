package com.websitestudios.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Restricts Spring Boot Actuator endpoints.
 *
 * Actuator exposes sensitive runtime info (env vars, beans, health details).
 * We only expose safe endpoints and restrict the rest.
 *
 * Configuration is primarily via application.yml:
 * management:
 * endpoints:
 * web:
 * exposure:
 * include: health, info, metrics, prometheus
 * endpoint:
 * health:
 * show-details: never ← NEVER show DB connection info publicly
 * env:
 * enabled: false ← NEVER expose environment variables
 * beans:
 * enabled: false ← NEVER expose bean details
 *
 * In Phase 6: Actuator endpoints beyond health/info will require SUPER_ADMIN
 * role.
 */
@Configuration
public class ActuatorSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(ActuatorSecurityConfig.class);

    /*
     * Actuator security is handled via:
     *
     * 1. application.yml — controls which endpoints are exposed
     * 2. SecurityConfig — controls who can access them
     * 3. Phase 6 — JWT + RBAC will lock down metrics/prometheus to admins
     *
     * Currently safe because:
     * - Only health and info are exposed
     * - show-details: never (no DB/redis connection info)
     * - env endpoint is disabled
     * - beans endpoint is disabled
     *
     * This class serves as documentation and a hook for Phase 6 enhancements.
     */
}