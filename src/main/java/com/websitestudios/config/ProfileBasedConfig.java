package com.websitestudios.config;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * Profile-based configuration that toggles features between dev and prod.
 *
 * Profiles:
 * dev (default):
 * - Swagger UI: enabled
 * - Debug logging: enabled
 * - HTTPS enforcement: disabled
 * - Detailed error messages: enabled
 *
 * prod:
 * - Swagger UI: disabled (returns 404)
 * - Debug logging: disabled
 * - HTTPS enforcement: enabled
 * - Detailed error messages: disabled (safe generic messages only)
 *
 * Activate profile:
 * application.yml: spring.profiles.active: dev
 * Command line: java -jar app.jar --spring.profiles.active=prod
 * Environment: SPRING_PROFILES_ACTIVE=prod
 */
@Configuration
public class ProfileBasedConfig {

    private static final Logger log = LoggerFactory.getLogger(ProfileBasedConfig.class);

    private final Environment environment;

    @Value("${spring.application.name:website-studios-api}")
    private String applicationName;

    public ProfileBasedConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void logActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();

        if (activeProfiles.length == 0) {
            log.info("═══════════════════════════════════════════════");
            log.info("  {} started with DEFAULT profile", applicationName);
            log.info("  Swagger UI: ENABLED");
            log.info("  HTTPS Enforcement: DISABLED");
            log.info("═══════════════════════════════════════════════");
        } else {
            log.info("═══════════════════════════════════════════════");
            log.info("  {} started", applicationName);
            log.info("  Active Profiles: {}", Arrays.toString(activeProfiles));

            if (isProduction()) {
                log.info("  Mode: PRODUCTION");
                log.info("  Swagger UI: DISABLED");
                log.info("  HTTPS Enforcement: ENABLED");
                log.info("  Debug Logging: DISABLED");
            } else {
                log.info("  Mode: DEVELOPMENT");
                log.info("  Swagger UI: ENABLED");
                log.info("  HTTPS Enforcement: DISABLED");
                log.info("  Debug Logging: ENABLED");
            }

            log.info("═══════════════════════════════════════════════");
        }
    }

    /**
     * Check if current profile is production.
     */
    public boolean isProduction() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    /**
     * Check if current profile is development.
     */
    public boolean isDevelopment() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length == 0
                || Arrays.asList(profiles).contains("dev")
                || Arrays.asList(profiles).contains("default");
    }
}