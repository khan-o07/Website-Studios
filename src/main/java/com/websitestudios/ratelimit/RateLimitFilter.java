package com.websitestudios.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websitestudios.exception.WsErrorResponse;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate Limiting Filter — enforces per-IP request throttling.
 *
 * Applied AFTER sanitization and security headers, BEFORE JWT auth.
 *
 * Three tiers of limits:
 * FORM → POST /api/v1/project-requests (5 req/min) — strictest
 * LOGIN → POST /api/v1/auth/login (3 req/min) — brute-force guard
 * PUBLIC → All other public endpoints (60 req/min) — general
 *
 * Admin endpoints (JWT-protected) are NOT rate-limited here —
 * they're protected by authentication itself.
 *
 * Response on limit exceeded:
 * HTTP 429 Too Many Requests
 * Header: X-RateLimit-Remaining: 0
 * Header: Retry-After: <seconds>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitConfig rateLimitConfig;
    private final RateLimitProperties rateLimitProperties;
    private final IpAddressUtil ipAddressUtil;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitConfig rateLimitConfig,
            RateLimitProperties rateLimitProperties,
            IpAddressUtil ipAddressUtil,
            ObjectMapper objectMapper) {
        this.rateLimitConfig = rateLimitConfig;
        this.rateLimitProperties = rateLimitProperties;
        this.ipAddressUtil = ipAddressUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();

        // Skip OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Skip if rate limiting is disabled (e.g., in tests)
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String ipAddress = ipAddressUtil.extractClientIp(request);
        String path = request.getServletPath();
        String method = request.getMethod();

        // ──── Determine which bucket to use ────
        Bucket bucket = resolveBucket(path, method, ipAddress);

        if (bucket == null) {
            // No rate limiting for this path/method combo
            filterChain.doFilter(request, response);
            return;
        }

        // ──── Try to consume a token ────
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Token consumed successfully — add remaining info headers
            response.addHeader("X-RateLimit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);

        } else {
            // Rate limit exceeded
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;

            log.warn("Rate limit exceeded for IP: {} on path: {} {} — retry after {}s",
                    ipAddress, method, path, waitSeconds);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-RateLimit-Remaining", "0");
            response.addHeader("Retry-After", String.valueOf(waitSeconds));

            WsErrorResponse errorResponse = WsErrorResponse.of(
                    429,
                    "Too Many Requests",
                    "Rate limit exceeded. Please try again in " + waitSeconds + " seconds.");
            errorResponse.setPath(path);

            objectMapper.writeValue(response.getOutputStream(), errorResponse);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // BUCKET RESOLVER
    // ════════════════════════════════════════════════════════════════

    /**
     * Determine which rate limit bucket to use based on path and method.
     *
     * Returns null for paths that should not be rate-limited
     * (e.g., already-authenticated admin endpoints).
     */
    private Bucket resolveBucket(String path, String method, String ipAddress) {

        // STRICTEST: Form submission
        if ("POST".equalsIgnoreCase(method) && "/api/v1/project-requests".equals(path)) {
            log.debug("Applying FORM rate limit for IP: {}", ipAddress);
            return rateLimitConfig.resolveFormSubmitBucket(ipAddress);
        }

        // STRICT: Login endpoint
        if ("POST".equalsIgnoreCase(method) && "/api/v1/auth/login".equals(path)) {
            log.debug("Applying LOGIN rate limit for IP: {}", ipAddress);
            return rateLimitConfig.resolveLoginBucket(ipAddress);
        }

        // PUBLIC API: General public endpoints
        if (path.startsWith("/api/v1/country-codes")
                || path.startsWith("/api/v1/health")
                || path.startsWith("/api/v1/auth/refresh")) {
            log.debug("Applying PUBLIC rate limit for IP: {}", ipAddress);
            return rateLimitConfig.resolvePublicApiBucket(ipAddress);
        }

        // No rate limiting for admin endpoints, Swagger, Actuator
        return null;
    }
}