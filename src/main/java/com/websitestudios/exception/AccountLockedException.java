package com.websitestudios.exception;

import java.time.Instant;

/**
 * Thrown when an admin account is locked due to excessive failed login
 * attempts.
 * Maps to HTTP 423 Locked.
 *
 * Will be actively used in Phase 6 (Account Lockout).
 *
 * Usage:
 * throw new AccountLockedException("Account locked until 2025-01-15T11:00:00",
 * lockExpiresAt);
 */
public class AccountLockedException extends RuntimeException {

    private final Instant lockExpiresAt;

    public AccountLockedException(String message) {
        super(message);
        this.lockExpiresAt = null;
    }

    public AccountLockedException(String message, Instant lockExpiresAt) {
        super(message);
        this.lockExpiresAt = lockExpiresAt;
    }

    public Instant getLockExpiresAt() {
        return lockExpiresAt;
    }
}