package com.websitestudios.security.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Authentication response DTO — returned after successful login.
 *
 * Response:
 * {
 * "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
 * "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
 * "tokenType": "Bearer",
 * "accessTokenExpiresIn": 900,
 * "refreshTokenExpiresIn": 604800,
 * "username": "admin",
 * "role": "ADMIN"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long accessTokenExpiresIn; // seconds
    private long refreshTokenExpiresIn; // seconds
    private String username;
    private String role;

    // ──────────────────────────── Constructors ────────────────────────────

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String accessToken, String refreshToken,
            long accessTokenExpiresIn, long refreshTokenExpiresIn,
            String username, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        this.username = username;
        this.role = role;
    }

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }

    public void setAccessTokenExpiresIn(long accessTokenExpiresIn) {
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    public long getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }

    public void setRefreshTokenExpiresIn(long refreshTokenExpiresIn) {
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "AuthResponseDTO{username='" + username + "', role='" + role +
                "', tokenType='" + tokenType + "'}";
    }
}