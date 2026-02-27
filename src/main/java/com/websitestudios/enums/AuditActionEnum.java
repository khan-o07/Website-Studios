package com.websitestudios.enums;

/**
 * All auditable actions performed by admins in the system.
 * Stored in the audit_trail table for compliance and security review.
 */
public enum AuditActionEnum {

    // Project Request actions
    VIEW_REQUEST,
    STATUS_CHANGE,
    SOFT_DELETE,
    EXPORT_DATA,

    // Authentication actions
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    TOKEN_REFRESH,

    // Account management
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    PASSWORD_CHANGE,

    // Admin user management (SUPER_ADMIN only)
    CREATE_ADMIN,
    UPDATE_ADMIN,
    DISABLE_ADMIN,

    // Data access
    VIEW_AUDIT_TRAIL,
    VIEW_DASHBOARD,

    // Suspicious activity
    SUSPICIOUS_CAPTCHA,
    RATE_LIMIT_EXCEEDED,
    INVALID_TOKEN_ATTEMPT
}