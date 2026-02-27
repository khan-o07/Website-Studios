package com.websitestudios.security.rbac;

import com.websitestudios.enums.StudioRoleEnum;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps roles to their permitted actions.
 *
 * ADMIN:
 * - Read project requests
 * - Update project request status
 * - View dashboard
 *
 * SUPER_ADMIN (inherits ADMIN + additional):
 * - Delete project requests (soft)
 * - Manage admin users
 * - View audit trail
 * - Export data
 */
@Component
public class RolePermissionMapping {

    private final Map<StudioRoleEnum, Set<PermissionEnum>> rolePermissions;

    public RolePermissionMapping() {
        Map<StudioRoleEnum, Set<PermissionEnum>> map = new EnumMap<>(StudioRoleEnum.class);

        // STUDIO_ADMIN permissions
        map.put(StudioRoleEnum.STUDIO_ADMIN, EnumSet.of(
                PermissionEnum.READ_REQUESTS,
                PermissionEnum.WRITE_REQUESTS,
                PermissionEnum.VIEW_DASHBOARD));

        // STUDIO_SUPER_ADMIN permissions (all permissions)
        map.put(StudioRoleEnum.STUDIO_SUPER_ADMIN, EnumSet.allOf(PermissionEnum.class));

        this.rolePermissions = Collections.unmodifiableMap(map);
    }

    /**
     * Get permissions for a role.
     */
    public Set<PermissionEnum> getPermissions(StudioRoleEnum role) {
        return rolePermissions.getOrDefault(role, Collections.emptySet());
    }

    /**
     * Check if a role has a specific permission.
     */
    public boolean hasPermission(StudioRoleEnum role, PermissionEnum permission) {
        Set<PermissionEnum> permissions = rolePermissions.get(role);
        return permissions != null && permissions.contains(permission);
    }
}