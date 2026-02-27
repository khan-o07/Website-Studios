package com.websitestudios.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom metrics for Website Studios API.
 *
 * All metrics are prefixed with "ws." for easy identification in
 * Prometheus/Grafana.
 *
 * COUNTERS (cumulative, always increasing):
 * ws.requests.form.submitted → Total form submissions
 * ws.requests.form.success → Successful submissions
 * ws.requests.form.duplicate → Duplicate rejections
 * ws.requests.form.captcha.failed → reCAPTCHA failures
 * ws.security.login.success → Successful logins
 * ws.security.login.failed → Failed logins
 * ws.security.account.locked → Account lockouts
 * ws.security.rate.limit.hit → Rate limit violations
 * ws.security.invalid.token → Invalid JWT attempts
 * ws.status.changes → Project status changes (by status)
 * ws.requests.deleted → Soft deletes
 *
 * TIMERS (request latency histograms):
 * ws.timer.form.submission → Form submission processing time
 * ws.timer.jwt.validation → JWT validation time
 *
 * GAUGES (current value snapshots):
 * ws.requests.pending → Current pending requests count
 * ws.requests.total → Total active requests
 */
@Component
public class WsMetricsService {

    private static final Logger log = LoggerFactory.getLogger(WsMetricsService.class);

    private final MeterRegistry meterRegistry;

    // ── Counters ──────────────────────────────────────────────────
    private final Counter formSubmittedCounter;
    private final Counter formSuccessCounter;
    private final Counter formDuplicateCounter;
    private final Counter captchaFailedCounter;
    private final Counter loginSuccessCounter;
    private final Counter loginFailedCounter;
    private final Counter accountLockedCounter;
    private final Counter rateLimitHitCounter;
    private final Counter invalidTokenCounter;
    private final Counter softDeleteCounter;

    // ── Timers ────────────────────────────────────────────────────
    private final Timer formSubmissionTimer;
    private final Timer jwtValidationTimer;

    // ── Gauge backing values ──────────────────────────────────────
    private final AtomicLong pendingRequestsGauge = new AtomicLong(0);
    private final AtomicLong totalRequestsGauge = new AtomicLong(0);

    public WsMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // ── Register Counters ──────────────────────────────────────
        this.formSubmittedCounter = Counter.builder("ws.requests.form.submitted")
                .description("Total form submissions received")
                .register(meterRegistry);

        this.formSuccessCounter = Counter.builder("ws.requests.form.success")
                .description("Successful form submissions saved to DB")
                .register(meterRegistry);

        this.formDuplicateCounter = Counter.builder("ws.requests.form.duplicate")
                .description("Form submissions rejected as duplicates")
                .register(meterRegistry);

        this.captchaFailedCounter = Counter.builder("ws.requests.form.captcha.failed")
                .description("Form submissions rejected due to reCAPTCHA failure")
                .register(meterRegistry);

        this.loginSuccessCounter = Counter.builder("ws.security.login.success")
                .description("Successful admin login events")
                .register(meterRegistry);

        this.loginFailedCounter = Counter.builder("ws.security.login.failed")
                .description("Failed admin login attempts")
                .register(meterRegistry);

        this.accountLockedCounter = Counter.builder("ws.security.account.locked")
                .description("Admin accounts locked due to failed login attempts")
                .register(meterRegistry);

        this.rateLimitHitCounter = Counter.builder("ws.security.rate.limit.hit")
                .description("Rate limit violations per endpoint")
                .register(meterRegistry);

        this.invalidTokenCounter = Counter.builder("ws.security.invalid.token")
                .description("Invalid or expired JWT token attempts")
                .register(meterRegistry);

        this.softDeleteCounter = Counter.builder("ws.requests.deleted")
                .description("Total soft-deleted project requests")
                .register(meterRegistry);

        // ── Register Timers ───────────────────────────────────────
        this.formSubmissionTimer = Timer.builder("ws.timer.form.submission")
                .description("Time taken to process a form submission end-to-end")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.jwtValidationTimer = Timer.builder("ws.timer.jwt.validation")
                .description("Time taken to validate a JWT token")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // ── Register Gauges ───────────────────────────────────────
        Gauge.builder("ws.requests.pending", pendingRequestsGauge, AtomicLong::get)
                .description("Current number of pending project requests")
                .register(meterRegistry);

        Gauge.builder("ws.requests.total", totalRequestsGauge, AtomicLong::get)
                .description("Total active (non-deleted) project requests")
                .register(meterRegistry);

        log.info("Website Studios custom metrics registered");
    }

    // ════════════════════════════════════════════════════════════════
    // COUNTER METHODS
    // ════════════════════════════════════════════════════════════════

    public void incrementFormSubmitted() {
        formSubmittedCounter.increment();
    }

    public void incrementFormSuccess() {
        formSuccessCounter.increment();
    }

    public void incrementFormDuplicate() {
        formDuplicateCounter.increment();
    }

    public void incrementCaptchaFailed() {
        captchaFailedCounter.increment();
    }

    public void incrementLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public void incrementLoginFailed() {
        loginFailedCounter.increment();
    }

    public void incrementAccountLocked() {
        accountLockedCounter.increment();
    }

    public void incrementRateLimitHit() {
        rateLimitHitCounter.increment();
    }

    public void incrementInvalidToken() {
        invalidTokenCounter.increment();
    }

    public void incrementSoftDelete() {
        softDeleteCounter.increment();
    }

    /**
     * Increment a status change counter with a tag for the new status.
     * Produces: ws.status.changes{status="IN_PROGRESS"}
     */
    public void incrementStatusChange(String newStatus) {
        Counter.builder("ws.status.changes")
                .description("Project request status changes by new status")
                .tag("status", newStatus != null ? newStatus : "UNKNOWN")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Increment rate limit hit with path tag.
     * Produces: ws.security.rate.limit.hit{path="/api/v1/project-requests"}
     */
    public void incrementRateLimitHit(String path) {
        Counter.builder("ws.security.rate.limit.hit.path")
                .description("Rate limit violations by endpoint path")
                .tag("path", path != null ? path : "unknown")
                .register(meterRegistry)
                .increment();
        rateLimitHitCounter.increment();
    }

    // ════════════════════════════════════════════════════════════════
    // TIMER METHODS
    // ════════════════════════════════════════════════════════════════

    public Timer.Sample startFormSubmissionTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopFormSubmissionTimer(Timer.Sample sample) {
        sample.stop(formSubmissionTimer);
    }

    public Timer.Sample startJwtValidationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopJwtValidationTimer(Timer.Sample sample) {
        sample.stop(jwtValidationTimer);
    }

    // ════════════════════════════════════════════════════════════════
    // GAUGE METHODS
    // ════════════════════════════════════════════════════════════════

    public void setPendingRequests(long count) {
        pendingRequestsGauge.set(count);
    }

    public void setTotalRequests(long count) {
        totalRequestsGauge.set(count);
    }

    public void decrementPendingRequests() {
        pendingRequestsGauge.updateAndGet(v -> Math.max(0, v - 1));
    }

    public void incrementPendingRequests() {
        pendingRequestsGauge.incrementAndGet();
    }
}