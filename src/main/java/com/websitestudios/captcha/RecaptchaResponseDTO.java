package com.websitestudios.captcha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps Google reCAPTCHA API response.
 *
 * Google API returns:
 * {
 * "success": true,
 * "score": 0.9,
 * "action": "submit",
 * "challenge_ts": "2025-01-15T10:30:00Z",
 * "hostname": "websitestudios.com",
 * "error-codes": []
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecaptchaResponseDTO {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("score")
    private double score;

    @JsonProperty("action")
    private String action;

    @JsonProperty("challenge_ts")
    private String challengeTs;

    @JsonProperty("hostname")
    private String hostname;

    @JsonProperty("error-codes")
    private List<String> errorCodes;

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getChallengeTs() {
        return challengeTs;
    }

    public void setChallengeTs(String challengeTs) {
        this.challengeTs = challengeTs;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public List<String> getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(List<String> errorCodes) {
        this.errorCodes = errorCodes;
    }

    @Override
    public String toString() {
        return "RecaptchaResponseDTO{success=" + success + ", score=" + score +
                ", action='" + action + "', hostname='" + hostname + "'}";
    }
}