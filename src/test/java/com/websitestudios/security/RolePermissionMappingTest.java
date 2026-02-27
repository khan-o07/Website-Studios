package com.websitestudios.security;

import com.websitestudios.security.rbac.PermissionEnum;
import com.websitestudios.security.rbac.RolePermissionMapping;
import com.websitestudios.enums.StudioRoleEnum;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RolePermissionMappingTest {

    private final RolePermissionMapping mapper = new RolePermissionMapping();

    @Test
    void studioAdminHasExpectedPermissions() {
        Set<PermissionEnum> perms = mapper.getPermissions(StudioRoleEnum.STUDIO_ADMIN);
        assertTrue(perms.contains(PermissionEnum.READ_REQUESTS));
        assertTrue(perms.contains(PermissionEnum.WRITE_REQUESTS));
        assertTrue(perms.contains(PermissionEnum.VIEW_DASHBOARD));
        assertFalse(perms.contains(PermissionEnum.DELETE_REQUESTS));
    }

    @Test
    void studioSuperAdminHasAllPermissions() {
        Set<PermissionEnum> perms = mapper.getPermissions(StudioRoleEnum.STUDIO_SUPER_ADMIN);
        assertEquals(EnumSet.allOf(PermissionEnum.class), perms);
    }

    @Test
    void unknownRoleReturnsEmpty() {
        assertTrue(mapper.getPermissions(null).isEmpty());
        assertFalse(mapper.hasPermission(null, PermissionEnum.READ_REQUESTS));
    }
}