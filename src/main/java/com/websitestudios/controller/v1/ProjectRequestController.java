package com.websitestudios.controller.v1;

import com.websitestudios.dto.request.ProjectRequestDTO;
import com.websitestudios.dto.request.StatusUpdateDTO;
import com.websitestudios.dto.response.PaginatedResponseDTO;
import com.websitestudios.dto.response.ProjectRequestResponseDTO;
import com.websitestudios.dto.response.WsApiResponseDTO;
import com.websitestudios.entity.ProjectRequest;
import com.websitestudios.enums.ProjectStatusEnum;
import com.websitestudios.mapper.ProjectRequestMapper;
import com.websitestudios.service.ProjectRequestService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles project request (lead generation form) operations.
 *
 * PUBLIC endpoints:
 * POST /api/v1/project-requests → Submit a new project request
 *
 * ADMIN endpoints (TODO Phase 6: secure with JWT + RBAC):
 * GET /api/v1/project-requests → List all requests (paginated)
 * GET /api/v1/project-requests/{id} → Get single request detail
 * PUT /api/v1/project-requests/{id}/status → Update status
 * DELETE /api/v1/project-requests/{id} → Soft delete
 */
@RestController
@RequestMapping("/api/v1/project-requests")
public class ProjectRequestController {

    private static final Logger log = LoggerFactory.getLogger(ProjectRequestController.class);

    private final ProjectRequestService projectRequestService;
    private final ProjectRequestMapper projectRequestMapper;

    public ProjectRequestController(ProjectRequestService projectRequestService,
            ProjectRequestMapper projectRequestMapper) {
        this.projectRequestService = projectRequestService;
        this.projectRequestMapper = projectRequestMapper;
    }

    // ════════════════════════════════════════════════════════════════
    // PUBLIC: Submit a new project request
    // ════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/project-requests
     *
     * Submission flow:
     * 1. @Valid triggers JSR-303 validation on DTO
     * 2. Service layer: reCAPTCHA check (TODO Phase 7)
     * 3. Service layer: Duplicate check (email_hash + phone_hash)
     * 4. Service layer: Sanitize → Encrypt → Hash → Save
     * 5. Return masked response
     *
     * Responses:
     * 201 Created → Success
     * 400 Bad Request → Validation failed (Phase 4: GlobalExceptionHandler)
     * 409 Conflict → Duplicate submission (Phase 4: DuplicateRequestException)
     * 429 Too Many → Rate limited (Phase 7)
     */
    @PostMapping
    public ResponseEntity<WsApiResponseDTO<ProjectRequestResponseDTO>> submitProjectRequest(
            @Valid @RequestBody ProjectRequestDTO requestDTO) {

        log.info("Received project request submission: {}", requestDTO);

        // Map DTO → Entity (structural fields only)
        ProjectRequest entity = projectRequestMapper.toEntity(requestDTO);

        // Delegate to service: sanitize, encrypt, hash, duplicate check, save
        ProjectRequest savedEntity = projectRequestService.createProjectRequest(
                entity,
                requestDTO.getEmail(),
                requestDTO.getPhoneNumber(),
                requestDTO.getRecaptchaToken());

        // Map Entity → Masked Public Response
        ProjectRequestResponseDTO responseDTO = projectRequestMapper.toPublicResponseDTO(savedEntity);

        log.info("Project request created successfully with ID: {}", savedEntity.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(WsApiResponseDTO.success("Project request submitted successfully", responseDTO));
    }

    // ════════════════════════════════════════════════════════════════
    // ADMIN: List all project requests (paginated)
    // ════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/project-requests?page=0&size=20&status=PENDING
     *
     * Returns paginated list of all project requests.
     * Admin sees full decrypted data.
     *
     * Query params:
     * page → Page number (default: 0)
     * size → Page size (default: 20, max: 100)
     * status → Optional filter by status (PENDING, IN_PROGRESS, etc.)
     *
     * TODO Phase 6: @PreAuthorize("hasRole('ADMIN')")
     */
    @GetMapping
    public ResponseEntity<WsApiResponseDTO<PaginatedResponseDTO<ProjectRequestResponseDTO>>> getAllProjectRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        log.info("Admin fetching project requests - page: {}, size: {}, status: {}", page, size, status);

        // Clamp page size to prevent abuse
        if (size > 100) {
            size = 100;
        }
        if (size < 1) {
            size = 20;
        }
        if (page < 0) {
            page = 0;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ProjectRequest> entityPage;

        if (status != null && !status.isBlank()) {
            try {
                ProjectStatusEnum statusEnum = ProjectStatusEnum.valueOf(status.toUpperCase().trim());
                entityPage = projectRequestService.getProjectRequestsByStatus(statusEnum, pageable);
                log.info("Filtering by status: {}", statusEnum);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter provided: {}", status);
                return ResponseEntity
                        .badRequest()
                        .body(WsApiResponseDTO.error("Invalid status value: " + status));
            }
        } else {
            entityPage = projectRequestService.getAllProjectRequests(pageable);
        }

        // Map entities → admin response DTOs (decrypted)
        Page<ProjectRequestResponseDTO> dtoPage = entityPage.map(projectRequestMapper::toAdminResponseDTO);

        PaginatedResponseDTO<ProjectRequestResponseDTO> paginatedResponse = PaginatedResponseDTO.from(dtoPage);

        log.info("Returning {} project requests (page {} of {})",
                paginatedResponse.getContent().size(),
                paginatedResponse.getPageNumber(),
                paginatedResponse.getTotalPages());

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Project requests retrieved successfully", paginatedResponse));
    }

    // ════════════════════════════════════════════════════════════════
    // ADMIN: Get single project request by ID
    // ════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/project-requests/{id}
     *
     * Returns a single project request with full decrypted data.
     *
     * Responses:
     * 200 OK → Found
     * 404 Not Found → ID doesn't exist (Phase 4: ResourceNotFoundException)
     *
     * TODO Phase 6: @PreAuthorize("hasRole('ADMIN')")
     */
    @GetMapping("/{id}")
    public ResponseEntity<WsApiResponseDTO<ProjectRequestResponseDTO>> getProjectRequestById(
            @PathVariable Long id) {

        log.info("Admin fetching project request with ID: {}", id);

        ProjectRequest entity = projectRequestService.getProjectRequestById(id);

        ProjectRequestResponseDTO responseDTO = projectRequestMapper.toAdminResponseDTO(entity);

        log.info("Returning project request ID: {}", id);

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Project request retrieved successfully", responseDTO));
    }

    // ════════════════════════════════════════════════════════════════
    // ADMIN: Update project request status
    // ════════════════════════════════════════════════════════════════

    /**
     * PUT /api/v1/project-requests/{id}/status
     *
     * Updates the status of a project request.
     *
     * Request body:
     * { "status": "IN_PROGRESS" }
     *
     * Responses:
     * 200 OK → Status updated
     * 400 Bad Request → Invalid status value
     * 404 Not Found → ID doesn't exist
     *
     * TODO Phase 6: @PreAuthorize("hasRole('ADMIN')")
     * TODO Phase 8: AuditTrailService.log(admin, STATUS_CHANGE, oldStatus,
     * newStatus)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<WsApiResponseDTO<ProjectRequestResponseDTO>> updateProjectRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO statusUpdateDTO) {

        log.info("Admin updating status for project request ID: {} to status: {}",
                id, statusUpdateDTO.getStatus());

        // Parse and validate status enum
        ProjectStatusEnum newStatus;
        try {
            newStatus = ProjectStatusEnum.valueOf(statusUpdateDTO.getStatus().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status value provided: {}", statusUpdateDTO.getStatus());
            return ResponseEntity
                    .badRequest()
                    .body(WsApiResponseDTO.error(
                            "Invalid status value: " + statusUpdateDTO.getStatus() +
                                    ". Allowed values: PENDING, IN_PROGRESS, COMPLETED, CANCELLED"));
        }

        ProjectRequest updatedEntity = projectRequestService.updateProjectRequestStatus(id, newStatus);

        ProjectRequestResponseDTO responseDTO = projectRequestMapper.toAdminResponseDTO(updatedEntity);

        log.info("Project request ID: {} status updated to: {}", id, newStatus);

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Status updated successfully", responseDTO));
    }

    // ════════════════════════════════════════════════════════════════
    // SUPER_ADMIN: Soft delete a project request
    // ════════════════════════════════════════════════════════════════

    /**
     * DELETE /api/v1/project-requests/{id}
     *
     * Soft deletes a project request (sets is_deleted = true).
     * Data is NOT removed from the database.
     *
     * Responses:
     * 200 OK → Soft deleted
     * 404 Not Found → ID doesn't exist
     *
     * TODO Phase 6: @PreAuthorize("hasRole('SUPER_ADMIN')")
     * TODO Phase 8: AuditTrailService.log(admin, DELETE, id)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<WsApiResponseDTO<Void>> softDeleteProjectRequest(
            @PathVariable Long id) {

        log.info("Super Admin soft-deleting project request ID: {}", id);

        projectRequestService.softDeleteProjectRequest(id);

        log.info("Project request ID: {} soft-deleted successfully", id);

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Project request deleted successfully"));
    }
}