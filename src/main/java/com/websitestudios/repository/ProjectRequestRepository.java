package com.websitestudios.repository;

import com.websitestudios.entity.ProjectRequest;
import com.websitestudios.enums.ProjectStatusEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ProjectRequest entity.
 * All queries automatically filter out soft-deleted records.
 *
 * Uses parameterized queries (Spring Data JPA) — SQL injection safe.
 */
@Repository
public interface ProjectRequestRepository extends JpaRepository<ProjectRequest, Long> {

    /**
     * Check if a non-deleted request exists with matching email AND phone hashes.
     * Used for duplicate detection.
     */
    boolean existsByEmailHashAndPhoneHashAndIsDeletedFalse(String emailHash, String phoneHash);

    /**
     * Find a non-deleted request by ID.
     */
    Optional<ProjectRequest> findByIdAndIsDeletedFalse(Long id);

    /**
     * Find all non-deleted requests (paginated).
     */
    Page<ProjectRequest> findByIsDeletedFalse(Pageable pageable);

    /**
     * Find non-deleted requests filtered by status (paginated).
     */
    Page<ProjectRequest> findByStatusAndIsDeletedFalse(ProjectStatusEnum status, Pageable pageable);

    /**
     * Count all non-deleted requests.
     */
    long countByIsDeletedFalse();

    /**
     * Count non-deleted requests by status.
     */
    long countByStatusAndIsDeletedFalse(ProjectStatusEnum status);
}