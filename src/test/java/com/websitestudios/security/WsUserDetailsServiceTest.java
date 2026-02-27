package com.websitestudios.security;

import com.websitestudios.entity.StudioAdmin;
import com.websitestudios.entity.StudioRole;
import com.websitestudios.enums.StudioRoleEnum;
import com.websitestudios.repository.StudioAdminRepository;
import com.websitestudios.security.auth.WsUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WsUserDetailsServiceTest {

    @Mock
    private StudioAdminRepository repository;

    @InjectMocks
    private WsUserDetailsService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_success() {
        StudioRole role = StudioRole.builder()
                .roleName(StudioRoleEnum.STUDIO_ADMIN)
                .build();
        StudioAdmin admin = StudioAdmin.builder()
                .username("testuser")
                .passwordHash("hashedpwd")
                .isActive(true)
                .isLocked(false)
                .role(role)
                .build();

        when(repository.findByUsername("testuser")).thenReturn(Optional.of(admin));

        UserDetails details = service.loadUserByUsername("testuser");

        assertEquals("testuser", details.getUsername());
        assertEquals("hashedpwd", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> "ROLE_STUDIO_ADMIN".equals(a.getAuthority())));
        assertTrue(details.isAccountNonLocked(), "Account should not be marked locked");
        assertTrue(details.isEnabled());
    }

    @Test
    void loadUserByUsername_notFound() {
        when(repository.findByUsername("nope")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nope"));
    }
}