package com.websitestudios.enums;

/**
 * Defines the roles available for Website Studios admin users.
 * Used for Role-Based Access Control (RBAC).
 */
public enum StudioRoleEnum {

    STUDIO_ADMIN("Studio Admin"),
    STUDIO_SUPER_ADMIN("Studio Super Admin");

    private final String displayName;

    StudioRoleEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}