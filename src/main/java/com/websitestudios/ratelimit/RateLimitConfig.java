package com.websitestudios.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bucket4j rate limit configuration.
 *
 * Uses the Token Bucket algorithm:
 * - Each IP gets a "bucket" with N tokens
 * - Each request consumes 1 token
 * - Tokens refill at a fixed rate
 * - When bucket is empty → 429 Too Many Requests
 *
 * Bucket types:
 * PUBLIC → 60 requests/minute (general API browsing)
 * FORM → 5 requests/minute (form submission, strict)
 * LOGIN → 3 requests/minute (brute-force protection)
 *
 * Storage: In-memory ConcurrentHashMap (Phase 9 upgrade: Redis-backed).
 *
 * NOTE: In-memory works for single-instance deployments.
 * For multi-instance/clustered deployments, use Redis bucket store.
 * Redis integration is prepared in RedisConfig.java for Phase 9.
 */
@Configuration
public class RateLimitConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimitConfig.class);

    private final RateLimitProperties properties;

    // In-memory bucket stores — keyed by "TYPE:IP"
    private final Map<String, Bucket> publicApiBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> formSubmitBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();

    public RateLimitConfig(RateLimitProperties properties) {
        this.properties = properties;
        log.info("Rate Limit Config initialized — public: {}/min, form: {}/min, login: {}/min",
                properties.getPublicApiRequestsPerMinute(),
                properties.getFormSubmitRequestsPerMinute(),
                properties.getLoginRequestsPerMinute());
    }

    // ════════════════════════════════════════════════════════════════
    // BUCKET RESOLVERS — Get or create bucket for a given IP
    // ════════════════════════════════════════════════════════════════

    /**
     * Get or create a PUBLIC API rate limit bucket for an IP.
     * Used for: /api/v1/country-codes, /api/v1/health, general browsing.
     */
    public Bucket resolvePublicApiBucket(String ipAddress) {
        return publicApiBuckets.computeIfAbsent(
                "PUBLIC:" + ipAddress,
                key -> createBucket(properties.getPublicApiRequestsPerMinute(), Duration.ofMinutes(1)));
    }

    /**
     * Get or create a FORM SUBMIT rate limit bucket for an IP.
     * Used for: POST /api/v1/project-requests (strict limit).
     */
    public Bucket resolveFormSubmitBucket(String ipAddress) {
        return formSubmitBuckets.computeIfAbsent(
                "FORM:" + ipAddress,
                key -> createBucket(properties.getFormSubmitRequestsPerMinute(), Duration.ofMinutes(1)));
    }

    /**
     * Get or create a LOGIN rate limit bucket for an IP.
     * Used for: POST /api/v1/auth/login (brute-force protection).
     */
    public Bucket resolveLoginBucket(String ipAddress) {
        return loginBuckets.computeIfAbsent(
                "LOGIN:" + ipAddress,
                key -> createBucket(properties.getLoginRequestsPerMinute(), Duration.ofMinutes(1)));
    }

    // ════════════════════════════════════════════════════════════════
    // BUCKET FACTORY
    // ════════════════════════════════════════════════════════════════

    /**
     * Create a token bucket with greedy refill.
     *
     * @param capacity     Maximum tokens (= max requests in the window)
     * @param refillPeriod Time window for refill
     */
    private Bucket createBucket(int capacity, Duration refillPeriod) {
        Bandwidth limit = Bandwidth.simple(
                capacity,
                refillPeriod);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // ════════════════════════════════════════════════════════════════
    // BUCKET CLEANUP (Optional — prevents memory leak over time)
    // ════════════════════════════════════════════════════════════════

    /**
     * Clear all buckets — useful for testing or scheduled cleanup.
     * In production, implement a scheduled task to evict stale entries.
     */
    public void clearAllBuckets() {
        publicApiBuckets.clear();
        formSubmitBuckets.clear();
        loginBuckets.clear();
        log.info("All rate limit buckets cleared");
    }

    /**
     * Get current bucket counts for monitoring.
     */
    public Map<String, Integer> getBucketStats() {
        return Map.of(
                "publicBuckets", publicApiBuckets.size(),
                "formBuckets", formSubmitBuckets.size(),
                "loginBuckets", loginBuckets.size());
    }
}