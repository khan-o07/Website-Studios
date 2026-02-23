package com.websitestudios.service;

import com.websitestudios.entity.ProjectRequest;
import com.websitestudios.enums.ProjectStatusEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for project request operations.
 * Updated in Phase 3 to support controller requirements.
 */
public interface ProjectRequestService {

    /**
     * Create a new project request.
     * Handles: sanitization, encryption, hashing, duplicate check, save.
     *
     * @param entity       Mapped entity (structural fields from mapper)
     * @param rawEmail     Raw email (for encryption + hashing)
     * @param rawPhone     Raw phone (for encryption + hashing)
     * @param captchaToken reCAPTCHA token (TODO Phase 7: verify)
     * @return Saved entity with generated ID
     */
    ProjectRequest createProjectRequest(ProjectRequest entity, String rawEmail,
            String rawPhone, String captchaToken);

    /**
     * Get a single project request by ID.
     * Throws ResourceNotFoundException if not found.
     */
    ProjectRequest getProjectRequestById(Long id);

    /**
     * Get all non-deleted project requests (paginated).
     */
    Page<ProjectRequest> getAllProjectRequests(Pageable pageable);

    /**
     * Get project requests filtered by status (paginated).
     */
    Page<ProjectRequest> getProjectRequestsByStatus(ProjectStatusEnum status, Pageable pageable);

    /**
     * Update the status of a project request.
     */
    ProjectRequest updateProjectRequestStatus(Long id, ProjectStatusEnum newStatus);

    /**
     * Soft delete a project request (sets is_deleted = true).
     */
    void softDeleteProjectRequest(Long id);

    /**
     * Get total count of non-deleted requests.
     */
    long getTotalCount();

    /**
     * Get count by status.
     */
    long getCountByStatus(String status);
}