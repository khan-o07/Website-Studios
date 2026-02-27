package com.websitestudios.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configurable rate limit properties loaded from application.yml.
 *
 * Configuration:
 * ws:
 * rate-limit:
 * public-api-requests-per-minute: 60
 * form-submit-requests-per-minute: 5
 * login-requests-per-minute: 3
 * same-contact-cooldown-minutes: 10
 */
@Component
@ConfigurationProperties(prefix = "ws.rate-limit")
public class RateLimitProperties {

    private int publicApiRequestsPerMinute = 60;
    private int formSubmitRequestsPerMinute = 5;
    private int loginRequestsPerMinute = 3;
    private int sameContactCooldownMinutes = 10;
    private boolean enabled = true;

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public int getPublicApiRequestsPerMinute() {
        return publicApiRequestsPerMinute;
    }

    public void setPublicApiRequestsPerMinute(int publicApiRequestsPerMinute) {
        this.publicApiRequestsPerMinute = publicApiRequestsPerMinute;
    }

    public int getFormSubmitRequestsPerMinute() {
        return formSubmitRequestsPerMinute;
    }

    public void setFormSubmitRequestsPerMinute(int formSubmitRequestsPerMinute) {
        this.formSubmitRequestsPerMinute = formSubmitRequestsPerMinute;
    }

    public int getLoginRequestsPerMinute() {
        return loginRequestsPerMinute;
    }

    public void setLoginRequestsPerMinute(int loginRequestsPerMinute) {
        this.loginRequestsPerMinute = loginRequestsPerMinute;
    }

    public int getSameContactCooldownMinutes() {
        return sameContactCooldownMinutes;
    }

    public void setSameContactCooldownMinutes(int sameContactCooldownMinutes) {
        this.sameContactCooldownMinutes = sameContactCooldownMinutes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}