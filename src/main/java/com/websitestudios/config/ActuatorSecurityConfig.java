package com.websitestudios.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Actuator endpoint security — locks down all management endpoints.
 *
 * Public (no auth):
 * /actuator/health → Used by Kubernetes, load balancers, uptime monitors
 * /actuator/info → Non-sensitive build info
 *
 * Requires SUPER_ADMIN:
 * /actuator/prometheus → Prometheus metrics scrape endpoint
 * /actuator/metrics → All metrics data
 * /actuator/env → Environment variables (sensitive!)
 * /actuator/loggers → Log level management
 * /actuator/threaddump → Thread dump (sensitive)
 * /actuator/heapdump → Heap dump (very sensitive)
 *
 * Blocked entirely:
 * /actuator/shutdown → NEVER expose this
 *
 * @Order(1) — This SecurityFilterChain runs BEFORE the main SecurityConfig
 * chain.
 * It handles all requests to /actuator/** exclusively.
 */
@Configuration
@Order(1)
public class ActuatorSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(ActuatorSecurityConfig.class);

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Actuator endpoint security");

        http
                .securityMatcher(EndpointRequest.toAnyEndpoint())
                .authorizeHttpRequests(auth -> auth

                        // ── Completely blocked ───────────────────────
                        .requestMatchers(EndpointRequest.to("shutdown"))
                        .denyAll()

                        // ── Public endpoints ─────────────────────────
                        .requestMatchers(EndpointRequest.to("health", "info"))
                        .permitAll()

                        // ── Prometheus — allows scraping from local network only ──
                        // In production: Prometheus pod accesses this directly
                        // Add IP-based restriction at Nginx layer for extra security
                        .requestMatchers(EndpointRequest.to("prometheus"))
                        .hasRole("SUPER_ADMIN")

                        // ── All other actuator endpoints ─────────────
                        .anyRequest()
                        .hasRole("SUPER_ADMIN"))
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}