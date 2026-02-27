package com.websitestudios.security.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh token request DTO.
 *
 * Request body:
 * {
 * "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
 * }
 */
public class RefreshTokenDTO {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    // ──────────────────────────── Constructors ────────────────────────────

    public RefreshTokenDTO() {
    }

    public RefreshTokenDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "RefreshTokenDTO{refreshToken='[PROTECTED]'}";
    }
}