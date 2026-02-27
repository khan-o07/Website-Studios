package com.websitestudios.repository;

import com.websitestudios.entity.AuditTrail;
import com.websitestudios.enums.AuditActionEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditTrail — append-only queries.
 * NO update or delete methods — audit records are permanent.
 */
@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {

    // ── Paginated queries for admin panel ──────────────────────────

    Page<AuditTrail> findAllByOrderByPerformedAtDesc(Pageable pageable);

    Page<AuditTrail> findByAdminUser_UsernameOrderByPerformedAtDesc(
            String username, Pageable pageable);

    Page<AuditTrail> findByActionOrderByPerformedAtDesc(
            AuditActionEnum action, Pageable pageable);

    Page<AuditTrail> findByTargetEntityAndTargetIdOrderByPerformedAtDesc(
            String targetEntity, Long targetId, Pageable pageable);

    // ── Time-range queries ─────────────────────────────────────────

    @Query("SELECT a FROM AuditTrail a WHERE a.performedAt BETWEEN :start AND :end " +
            "ORDER BY a.performedAt DESC")
    Page<AuditTrail> findByTimeRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    // ── Security monitoring queries ────────────────────────────────

    List<AuditTrail> findByIpAddressAndPerformedAtAfterOrderByPerformedAtDesc(
            String ipAddress, LocalDateTime after);

    long countByActionAndPerformedAtAfter(AuditActionEnum action, LocalDateTime after);

    @Query("SELECT a FROM AuditTrail a WHERE a.action IN " +
            "('LOGIN_FAILURE', 'RATE_LIMIT_EXCEEDED', 'SUSPICIOUS_CAPTCHA', " +
            "'INVALID_TOKEN_ATTEMPT', 'ACCOUNT_LOCKED') " +
            "AND a.performedAt > :since ORDER BY a.performedAt DESC")
    List<AuditTrail> findRecentSuspiciousActivity(@Param("since") LocalDateTime since);
}