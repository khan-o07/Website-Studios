package com.websitestudios.captcha;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Google reCAPTCHA v3 configuration properties.
 *
 * Configuration:
 * ws:
 * captcha:
 * secret-key: your-recaptcha-secret-key
 * site-key: your-recaptcha-site-key
 * verify-url: https://www.google.com/recaptcha/api/siteverify
 * min-score: 0.5
 * enabled: true
 */
@Component
@ConfigurationProperties(prefix = "ws.captcha")
public class RecaptchaProperties {

    private String secretKey;
    private String siteKey;
    private String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";
    private double minScore = 0.5;
    private boolean enabled = true;

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getVerifyUrl() {
        return verifyUrl;
    }

    public void setVerifyUrl(String verifyUrl) {
        this.verifyUrl = verifyUrl;
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}