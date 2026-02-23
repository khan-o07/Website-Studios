package com.websitestudios.repository;

import com.websitestudios.entity.ProjectRequest;
import com.websitestudios.enums.ProjectStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for ProjectRequest entity.
 * All queries use parameterized inputs (SQL injection safe).
 * Soft-deleted records are excluded by default where noted.
 */
@Repository
public interface ProjectRequestRepository extends JpaRepository<ProjectRequest, Long> {

    /**
     * Find a non-deleted project request by ID.
     */
    @Query("SELECT pr FROM ProjectRequest pr WHERE pr.id = :id AND pr.isDeleted = false")
    Optional<ProjectRequest> findActiveById(@Param("id") Long id);

    /**
     * Find all non-deleted project requests with pagination.
     */
    @Query("SELECT pr FROM ProjectRequest pr WHERE pr.isDeleted = false")
    Page<ProjectRequest> findAllActive(Pageable pageable);

    /**
     * Find all non-deleted project requests by status with pagination.
     */
    @Query("SELECT pr FROM ProjectRequest pr WHERE pr.status = :status AND pr.isDeleted = false")
    Page<ProjectRequest> findAllActiveByStatus(@Param("status") ProjectStatusEnum status, Pageable pageable);

    /**
     * Check if a non-deleted request exists with the same email and phone hash.
     * Used for duplicate detection.
     */
    @Query("SELECT COUNT(pr) > 0 FROM ProjectRequest pr WHERE pr.emailHash = :emailHash AND pr.phoneHash = :phoneHash AND pr.isDeleted = false")
    boolean existsByEmailHashAndPhoneHash(@Param("emailHash") String emailHash, @Param("phoneHash") String phoneHash);

    /**
     * Check if a non-deleted request with same email hash was created within given
     * time.
     * Used for duplicate throttling.
     */
    @Query("SELECT COUNT(pr) > 0 FROM ProjectRequest pr WHERE pr.emailHash = :emailHash AND pr.isDeleted = false AND pr.createdAt > :since")
    boolean existsRecentByEmailHash(@Param("emailHash") String emailHash, @Param("since") Instant since);

    /**
     * Count all non-deleted requests.
     */
    @Query("SELECT COUNT(pr) FROM ProjectRequest pr WHERE pr.isDeleted = false")
    long countActive();

    /**
     * Count non-deleted requests by status.
     */
    @Query("SELECT COUNT(pr) FROM ProjectRequest pr WHERE pr.status = :status AND pr.isDeleted = false")
    long countActiveByStatus(@Param("status") ProjectStatusEnum status);
}