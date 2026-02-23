package com.websitestudios.repository;

import com.websitestudios.entity.AuditTrail;
import com.websitestudios.enums.AuditActionEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for AuditTrail entity.
 * Append-only — records are never updated or deleted.
 */
@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {

    Page<AuditTrail> findAllByOrderByPerformedAtDesc(Pageable pageable);

    Page<AuditTrail> findByAdminIdOrderByPerformedAtDesc(Long adminId, Pageable pageable);

    Page<AuditTrail> findByActionOrderByPerformedAtDesc(AuditActionEnum action, Pageable pageable);

    Page<AuditTrail> findByTargetEntityAndTargetIdOrderByPerformedAtDesc(
            String targetEntity, Long targetId, Pageable pageable);
}