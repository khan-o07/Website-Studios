package com.websitestudios.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.stereotype.Component;

/**
 * Configures HTTP security response headers.
 *
 * These headers protect against:
 * - XSS (Cross-Site Scripting)
 * - Clickjacking (iframing)
 * - MIME type sniffing
 * - Protocol downgrade attacks
 * - Information leakage
 *
 * Headers set:
 * Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
 * X-Content-Type-Options: nosniff
 * X-Frame-Options: DENY
 * X-XSS-Protection: 0 (modern approach — rely on CSP instead)
 * Content-Security-Policy: default-src 'self'
 * Referrer-Policy: strict-origin-when-cross-origin
 * Permissions-Policy: camera=(), microphone=(), geolocation=()
 * Cache-Control: no-cache, no-store, max-age=0, must-revalidate
 */
@Component
public class SecurityHeadersConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityHeadersConfig.class);

    /**
     * Apply all security headers to the HttpSecurity headers configurer.
     * Called from SecurityConfig.
     */
    public void configure(HeadersConfigurer<?> headers) {

        log.info("Configuring security response headers");

        headers
                // ──── HSTS: Force HTTPS for 1 year ────
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000) // 1 year
                        .preload(true))

                // ──── Prevent MIME type sniffing ────
                .contentTypeOptions(contentType -> {
                    // Sets X-Content-Type-Options: nosniff
                    // Prevents browsers from MIME-sniffing a response away from declared
                    // content-type
                })

                // ──── Prevent clickjacking ────
                .frameOptions(frame -> frame.deny())

                // ──── XSS Protection ────
                // Modern approach: disable the legacy X-XSS-Protection header
                // and rely on Content-Security-Policy instead
                .xssProtection(xss -> xss.disable())

                // ──── Content Security Policy ────
                .contentSecurityPolicy(csp -> csp.policyDirectives(buildContentSecurityPolicy()))

                // ──── Referrer Policy ────
                .referrerPolicy(referrer -> referrer.policy(
                        org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))

                // ──── Cache Control ────
                .cacheControl(cache -> {
                    // Sets Cache-Control: no-cache, no-store, max-age=0, must-revalidate
                    // Prevents sensitive data from being cached
                })

                // ──── Permissions Policy ────
                .permissionsPolicy(permissions -> permissions.policy(buildPermissionsPolicy()));

        log.info("Security response headers configured successfully");
    }

    /**
     * Build Content Security Policy directives.
     *
     * This is an API-only backend, so we restrict everything to 'self'.
     * If serving any HTML (e.g., Swagger UI), adjust accordingly.
     */
    private String buildContentSecurityPolicy() {
        return String.join("; ",
                "default-src 'self'",
                "script-src 'self'",
                "style-src 'self' 'unsafe-inline'", // unsafe-inline needed for Swagger UI
                "img-src 'self' data:", // data: needed for Swagger UI
                "font-src 'self'",
                "connect-src 'self'",
                "frame-ancestors 'none'", // Prevent iframing (same as X-Frame-Options: DENY)
                "base-uri 'self'",
                "form-action 'self'",
                "object-src 'none'" // Block Flash, Java applets
        );
    }

    /**
     * Build Permissions Policy (formerly Feature-Policy).
     * Disables browser features we don't need — reduces attack surface.
     */
    private String buildPermissionsPolicy() {
        return String.join(", ",
                "camera=()",
                "microphone=()",
                "geolocation=()",
                "payment=()",
                "usb=()",
                "magnetometer=()",
                "gyroscope=()",
                "accelerometer=()");
    }
}