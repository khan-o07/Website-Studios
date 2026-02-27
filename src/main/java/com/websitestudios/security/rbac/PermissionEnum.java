package com.websitestudios.security.rbac;

/**
 * Fine-grained permissions for RBAC.
 *
 * Permissions map to specific actions within the system.
 * Roles are assigned sets of permissions via RolePermissionMapping.
 */
public enum PermissionEnum {

    // Project Request permissions
    READ_REQUESTS,
    WRITE_REQUESTS,
    DELETE_REQUESTS,

    // Admin management permissions
    MANAGE_USERS,
    VIEW_AUDIT_TRAIL,
    EXPORT_DATA,

    // Dashboard
    VIEW_DASHBOARD
}