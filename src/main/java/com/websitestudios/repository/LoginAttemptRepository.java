package com.websitestudios.repository;

import com.websitestudios.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Repository for LoginAttempt entity.
 * Used for tracking login attempts and enforcing lockout policy.
 */
@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    /**
     * Count failed login attempts for a username since a given time.
     */
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.username = :username AND la.success = false AND la.attemptedAt > :since")
    long countRecentFailedAttempts(@Param("username") String username, @Param("since") Instant since);

    /**
     * Count failed login attempts from an IP since a given time.
     */
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ip AND la.success = false AND la.attemptedAt > :since")
    long countRecentFailedAttemptsByIp(@Param("ip") String ipAddress, @Param("since") Instant since);
}