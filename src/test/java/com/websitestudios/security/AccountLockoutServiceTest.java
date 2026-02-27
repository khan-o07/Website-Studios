package com.websitestudios.security;

import com.websitestudios.entity.StudioAdmin;
import com.websitestudios.repository.LoginAttemptRepository;
import com.websitestudios.repository.StudioAdminRepository;
import com.websitestudios.security.lockout.AccountLockoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountLockoutServiceTest {

    @Mock
    private StudioAdminRepository adminRepo;

    @Mock
    private LoginAttemptRepository loginRepo;

    @InjectMocks
    private AccountLockoutService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void expiredLock_isUnlocked() {
        StudioAdmin admin = StudioAdmin.builder()
                .username("user1")
                .isLocked(true)
                .lockExpiresAt(Instant.now().minusSeconds(60))
                .build();
        when(adminRepo.findByUsername("user1")).thenReturn(Optional.of(admin));

        boolean locked = service.isAccountLocked("user1");
        assertFalse(locked, "Account with expired lock should not be considered locked");

        // After checking, the admin should be unlocked and saved
        assertFalse(admin.getIsLocked());
        verify(adminRepo).save(admin);
    }

    @Test
    void notLocked_returnsFalse() {
        StudioAdmin admin = StudioAdmin.builder()
                .username("user2")
                .isLocked(false)
                .build();
        when(adminRepo.findByUsername("user2")).thenReturn(Optional.of(admin));

        assertFalse(service.isAccountLocked("user2"));
        verify(adminRepo, never()).save(any());
    }

    @Test
    void unknownUser_notLocked() {
        when(adminRepo.findByUsername("missing")).thenReturn(Optional.empty());
        assertFalse(service.isAccountLocked("missing"));
    }
}