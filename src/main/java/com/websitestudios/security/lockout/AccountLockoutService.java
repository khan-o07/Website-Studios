package com.websitestudios.security.lockout;

import com.websitestudios.entity.LoginAttempt;
import com.websitestudios.entity.StudioAdmin;
import com.websitestudios.repository.LoginAttemptRepository;
import com.websitestudios.repository.StudioAdminRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Account Lockout Service — tracks failed login attempts and locks accounts.
 *
 * Rules:
 * - After N failed attempts → lock account for M minutes
 * - Successful login → reset counter
 * - Lock expires automatically after M minutes
 *
 * Configuration via application.yml:
 * ws:
 * security:
 * lockout:
 * max-failed-attempts: 5
 * lock-duration-minutes: 30
 *
 * All attempts are logged to the login_attempts table for security auditing.
 */
@Service
@Transactional
public class AccountLockoutService {

    private static final Logger log = LoggerFactory.getLogger(AccountLockoutService.class);

    @Value("${ws.security.lockout.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${ws.security.lockout.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    private final StudioAdminRepository studioAdminRepository;
    private final LoginAttemptRepository loginAttemptRepository;

    public AccountLockoutService(StudioAdminRepository studioAdminRepository,
            LoginAttemptRepository loginAttemptRepository) {
        this.studioAdminRepository = studioAdminRepository;
        this.loginAttemptRepository = loginAttemptRepository;
    }

    // ════════════════════════════════════════════════════════════════
    // CHECK LOCK STATUS
    // ════════════════════════════════════════════════════════════════

    /**
     * Check if an account is currently locked.
     * If the lock has expired, automatically unlock it.
     */
    public boolean isAccountLocked(String username) {
        Optional<StudioAdmin> adminOpt = studioAdminRepository.findByUsername(username);

        if (adminOpt.isEmpty()) {
            // User doesn't exist — don't reveal this fact
            // Return false so the auth flow can handle it with a generic error
            return false;
        }

        StudioAdmin admin = adminOpt.get();

        if (!admin.getIsLocked()) {
            return false;
        }

        // Check if lock has expired
        if (admin.getLockExpiresAt() != null && admin.getLockExpiresAt().isBefore(Instant.now())) {
            // Lock expired — auto-unlock
            log.info("Lock expired for user: {}, auto-unlocking", username);
            unlockAccount(admin);
            return false;
        }

        return true;
    }

    /**
     * Get the lock expiration time for a user.
     */
    public Instant getLockExpiresAt(String username) {
        return studioAdminRepository.findByUsername(username)
                .map(StudioAdmin::getLockExpiresAt)
                .orElse(null);
    }

    // ════════════════════════════════════════════════════════════════
    // RECORD ATTEMPTS
    // ════════════════════════════════════════════════════════════════

    /**
     * Record a failed login attempt.
     * If max attempts exceeded → lock the account.
     */
    public void recordFailedAttempt(String username, String ipAddress, String failureReason) {

        // Log to login_attempts table (always)
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccess(false);
        attempt.setFailureReason(failureReason);
        loginAttemptRepository.save(attempt);

        // Update failed_attempts counter on admin
        Optional<StudioAdmin> adminOpt = studioAdminRepository.findByUsername(username);

        if (adminOpt.isPresent()) {
            StudioAdmin admin = adminOpt.get();
            int newFailedCount = admin.getFailedAttempts() + 1;
            admin.setFailedAttempts(newFailedCount);

            log.warn("Failed login attempt #{} for user: {} from IP: {} — reason: {}",
                    newFailedCount, username, ipAddress, failureReason);

            // Check if we should lock the account
            if (newFailedCount >= maxFailedAttempts) {
                Instant lockUntil = Instant.now().plusSeconds(lockDurationMinutes * 60L);
                admin.setIsLocked(true);
                admin.setLockExpiresAt(lockUntil);

                log.warn("Account LOCKED for user: {} — {} failed attempts. Locked until: {}",
                        username, newFailedCount, lockUntil);
            }

            studioAdminRepository.save(admin);
        }
    }

    /**
     * Record a successful login.
     */
    public void recordSuccessfulLogin(String username, String ipAddress) {

        // Log to login_attempts table
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccess(true);
        loginAttemptRepository.save(attempt);

        // Update admin record
        Optional<StudioAdmin> adminOpt = studioAdminRepository.findByUsername(username);

        if (adminOpt.isPresent()) {
            StudioAdmin admin = adminOpt.get();
            admin.setLastLoginAt(Instant.now());
        }

        log.info("Successful login for user: {} from IP: {}", username, ipAddress);
    }

    // ════════════════════════════════════════════════════════════════
    // RESET / UNLOCK
    // ════════════════════════════════════════════════════════════════

    /**
     * Reset failed attempts counter after successful login.
     */
    public void resetFailedAttempts(String username) {
        Optional<StudioAdmin> adminOpt = studioAdminRepository.findByUsername(username);

        if (adminOpt.isPresent()) {
            StudioAdmin admin = adminOpt.get();

            if (admin.getFailedAttempts() > 0) {
                log.info("Resetting failed attempts for user: {} (was: {})",
                        username, admin.getFailedAttempts());
            }

            admin.setFailedAttempts(0);
            admin.setIsLocked(false);
            admin.setLockExpiresAt(null);
            studioAdminRepository.save(admin);
        }
    }

    /**
     * Unlock an account (internal use — when lock expires).
     */
    private void unlockAccount(StudioAdmin admin) {
        admin.setIsLocked(false);
        admin.setLockExpiresAt(null);
        admin.setFailedAttempts(0);
        studioAdminRepository.save(admin);

        log.info("Account unlocked: {}", admin.getUsername());
    }
}