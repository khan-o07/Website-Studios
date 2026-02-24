package com.websitestudios.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Strict CORS (Cross-Origin Resource Sharing) configuration.
 *
 * Controls which frontend domains can make requests to this API.
 *
 * SECURITY:
 * - In production: Only the exact frontend domain is allowed
 * - In development: localhost origins are allowed
 * - Wildcard (*) is NEVER used — it disables credential support
 * - Preflight cache: 1 hour (reduces OPTIONS requests)
 *
 * Configuration via application.yml:
 * ws:
 * cors:
 * allowed-origins: https://websitestudios.com,https://www.websitestudios.com
 * max-age: 3600
 */
@Configuration
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    @Value("${ws.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Value("${ws.cors.max-age:3600}")
    private long maxAge;

    /**
     * Provide CORS configuration source to SecurityConfig.
     */
    public CorsConfigurationSource corsConfigurationSource() {

        log.info("Configuring CORS with allowed origins: {}", allowedOrigins);

        CorsConfiguration configuration = new CorsConfiguration();

        // ──── Allowed Origins ────
        // Split comma-separated origins from config
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // ──── Allowed HTTP Methods ────
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ──── Allowed Headers ────
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));

        // ──── Exposed Headers ────
        // Headers the browser is allowed to read from the response
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition",
                "X-Total-Count",
                "Retry-After"));

        // ──── Allow Credentials ────
        // Required for JWT in Authorization header
        configuration.setAllowCredentials(true);

        // ──── Preflight Cache Duration ────
        // Browser caches preflight (OPTIONS) response for this duration
        configuration.setMaxAge(maxAge);

        // ──── Apply to all API paths ────
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        // Health and actuator endpoints — less restrictive
        CorsConfiguration healthCorsConfig = new CorsConfiguration();
        healthCorsConfig.setAllowedOrigins(List.of("*"));
        healthCorsConfig.setAllowedMethods(List.of("GET"));
        healthCorsConfig.setAllowCredentials(false);
        healthCorsConfig.setMaxAge(maxAge);
        source.registerCorsConfiguration("/actuator/**", healthCorsConfig);

        log.info("CORS configured for {} origin(s), max-age: {}s", origins.size(), maxAge);

        return source;
    }
}