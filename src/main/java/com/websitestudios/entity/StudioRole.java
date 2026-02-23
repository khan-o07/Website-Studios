package com.websitestudios.entity;

import com.websitestudios.enums.StudioRoleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents an authorization role in the Website Studios system.
 * Each StudioAdmin is assigned exactly one StudioRole.
 *
 * Roles define what operations an admin can perform (RBAC).
 *
 * Permissions are stored as a comma-separated string for
 * cross-database compatibility (PostgreSQL + H2).
 */
@Entity
@Table(name = "studio_roles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_name", columnNames = "role_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudioRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, length = 30)
    private StudioRoleEnum roleName;

    /**
     * Comma-separated list of permissions granted to this role.
     * Example: "READ,WRITE,DELETE,MANAGE_USERS"
     *
     * Stored as VARCHAR for cross-database compatibility (H2 + PostgreSQL).
     */
    @Column(name = "permissions", nullable = false, length = 500)
    private String permissions;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // ─── Helper Methods ───

    /**
     * Check if this role has a specific permission.
     */
    public boolean hasPermission(String permission) {
        if (permissions == null || permissions.isBlank())
            return false;
        String[] permArray = permissions.split(",");
        for (String p : permArray) {
            if (p.trim().equalsIgnoreCase(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get permissions as a String array.
     */
    public String[] getPermissionsArray() {
        if (permissions == null || permissions.isBlank()) {
            return new String[0];
        }
        return permissions.split(",");
    }
}