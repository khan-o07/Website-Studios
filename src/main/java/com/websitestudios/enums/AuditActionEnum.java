package com.websitestudios.enums;

/**
 * Tracks different types of admin actions for the audit trail.
 * Every admin operation is logged with one of these action types.
 */
public enum AuditActionEnum {

    // ─── Authentication Actions ───
    LOGIN("Admin Login"),
    LOGOUT("Admin Logout"),
    LOGIN_FAILED("Failed Login Attempt"),

    // ─── Project Request Actions ───
    VIEW_REQUEST("Viewed Project Request"),
    VIEW_ALL_REQUESTS("Viewed All Project Requests"),
    STATUS_CHANGE("Changed Project Status"),
    SOFT_DELETE("Soft Deleted Project Request"),

    // ─── Data Actions ───
    EXPORT_DATA("Exported Data"),

    // ─── Admin Management ───
    CREATE_ADMIN("Created Admin User"),
    UPDATE_ADMIN("Updated Admin User"),
    DISABLE_ADMIN("Disabled Admin User");

    private final String description;

    AuditActionEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}