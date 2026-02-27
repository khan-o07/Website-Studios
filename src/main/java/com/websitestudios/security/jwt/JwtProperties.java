package com.websitestudios.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties loaded from application.yml.
 *
 * Configuration:
 * ws:
 * security:
 * jwt:
 * secret-key: <base64-encoded-secret>
 * access-token-expiry-ms: 900000 (15 minutes)
 * refresh-token-expiry-ms: 604800000 (7 days)
 * issuer: website-studios-api
 */
@Component
@ConfigurationProperties(prefix = "ws.security.jwt")
public class JwtProperties {

    private String secretKey;
    private long accessTokenExpiryMs = 900000; // 15 minutes
    private long refreshTokenExpiryMs = 604800000; // 7 days
    private String issuer = "website-studios-api";

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getAccessTokenExpiryMs() {
        return accessTokenExpiryMs;
    }

    public void setAccessTokenExpiryMs(long accessTokenExpiryMs) {
        this.accessTokenExpiryMs = accessTokenExpiryMs;
    }

    public long getRefreshTokenExpiryMs() {
        return refreshTokenExpiryMs;
    }

    public void setRefreshTokenExpiryMs(long refreshTokenExpiryMs) {
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}