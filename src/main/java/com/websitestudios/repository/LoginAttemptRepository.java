package com.websitestudios.repository;

import com.websitestudios.entity.LoginAttempt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for LoginAttempt entity.
 * Used by AccountLockoutService for tracking login attempts.
 */
@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

        /**
         * Find failed login attempts for a user after a given timestamp.
         */
        List<LoginAttempt> findByUsernameAndSuccessFalseAndAttemptedAtAfter(
                        String username, Instant after);

        /**
         * Find all attempts by IP address after a given timestamp.
         */
        List<LoginAttempt> findByIpAddressAndAttemptedAtAfter(
                        String ipAddress, Instant after);

        /**
         * Count failed attempts for a user after a given timestamp.
         */
        long countByUsernameAndSuccessFalseAndAttemptedAtAfter(
                        String username, Instant after);

        /**
         * Count failed attempts from an IP after a given timestamp.
         */
        long countByIpAddressAndSuccessFalseAndAttemptedAtAfter(
                        String ipAddress, Instant after);
}