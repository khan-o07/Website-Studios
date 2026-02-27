package com.websitestudios.security.auth;

import com.websitestudios.entity.StudioAdmin;
import com.websitestudios.repository.StudioAdminRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Custom UserDetailsService — loads admin users from the database.
 *
 * Spring Security uses this to:
 * 1. Verify credentials during login
 * 2. Load user details during JWT validation
 *
 * Maps StudioAdmin entity → Spring Security UserDetails:
 * - username → UserDetails.username
 * - passwordHash → UserDetails.password
 * - studioRole.roleName → ROLE_ADMIN or ROLE_SUPER_ADMIN
 * - isActive + !isLocked → UserDetails.enabled
 */
@Service
public class WsUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(WsUserDetailsService.class);

    private final StudioAdminRepository studioAdminRepository;

    public WsUserDetailsService(StudioAdminRepository studioAdminRepository) {
        this.studioAdminRepository = studioAdminRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.debug("Loading user details for username: {}", username);

        StudioAdmin admin = studioAdminRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Admin user not found: {}", username);
                    return new UsernameNotFoundException(
                            "Admin user not found with username: " + username);
                });

        // Build role authority
        // StudioAdmin stores a StudioRole entity named "role" (not "studioRole").
        // The enum inside the role is the actual StudioRoleEnum value.
        String roleName = "ROLE_" + admin.getRole().getRoleName().name();

        log.debug("Found admin user: {}, role: {}, active: {}, locked: {}",
                admin.getUsername(), roleName, admin.getIsActive(), admin.getIsLocked());

        // Build Spring Security UserDetails
        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(roleName)))
                .accountExpired(false)
                .accountLocked(admin.getIsLocked())
                .credentialsExpired(false)
                .disabled(!admin.getIsActive())
                .build();
    }
}