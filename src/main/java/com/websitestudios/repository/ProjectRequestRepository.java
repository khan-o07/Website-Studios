package com.websitestudios.repository;

import com.websitestudios.entity.ProjectRequest;
import com.websitestudios.enums.ProjectStatusEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for ProjectRequest entity.
 * Updated in Phase 7: Added cooldown window query for
 * DuplicateRequestThrottler.
 */
@Repository
public interface ProjectRequestRepository extends JpaRepository<ProjectRequest, Long> {

    boolean existsByEmailHashAndPhoneHashAndIsDeletedFalse(String emailHash, String phoneHash);

    /**
     * NEW in Phase 7: Check for recent submission within a time window.
     * Used by DuplicateRequestThrottler for cooldown enforcement.
     */
    boolean existsByEmailHashAndPhoneHashAndIsDeletedFalseAndCreatedAtAfter(
            String emailHash, String phoneHash, Instant after);

    Optional<ProjectRequest> findByIdAndIsDeletedFalse(Long id);

    Page<ProjectRequest> findByIsDeletedFalse(Pageable pageable);

    Page<ProjectRequest> findByStatusAndIsDeletedFalse(ProjectStatusEnum status, Pageable pageable);

    long countByIsDeletedFalse();

    long countByStatusAndIsDeletedFalse(ProjectStatusEnum status);
}