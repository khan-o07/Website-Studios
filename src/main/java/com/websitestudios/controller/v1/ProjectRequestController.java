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
import com.websitestudios.service.AuditTrailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/project-requests")
public class ProjectRequestController {

    private static final Logger log = LoggerFactory.getLogger(ProjectRequestController.class);

    private final ProjectRequestService projectRequestService;
    private final ProjectRequestMapper projectRequestMapper;
    private final AuditTrailService auditTrailService;

    public ProjectRequestController(ProjectRequestService projectRequestService,
            ProjectRequestMapper projectRequestMapper,
            AuditTrailService auditTrailService) {
        this.projectRequestService = projectRequestService;
        this.projectRequestMapper = projectRequestMapper;
        this.auditTrailService = auditTrailService;
    }

    // ════════════════════════════════════════════════════════════════
    // PUBLIC: Submit a new project request
    // ════════════════════════════════════════════════════════════════

    @PostMapping
    public ResponseEntity<WsApiResponseDTO<ProjectRequestResponseDTO>> submitProjectRequest(
            @Valid @RequestBody ProjectRequestDTO requestDTO) {

        log.info("Received project request submission: {}", requestDTO);

        ProjectRequest entity = projectRequestMapper.toEntity(requestDTO);

        ProjectRequest savedEntity = projectRequestService.createProjectRequest(
                entity,
                requestDTO.getEmail(),
                requestDTO.getPhoneNumber(),
                requestDTO.getRecaptchaToken());

        ProjectRequestResponseDTO responseDTO = projectRequestMapper.toPublicResponseDTO(savedEntity);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(WsApiResponseDTO.success("Project request submitted successfully", responseDTO));
    }

    // ════════════════════════════════════════════════════════════════
    // ADMIN: List all project requests
    // ════════════════════════════════════════════════════════════════

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<WsApiResponseDTO<PaginatedResponseDTO<ProjectRequestResponseDTO>>> getAllProjectRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        if (size > 100)
            size = 100;
        if (size < 1)
            size = 20;
        if (page < 0)
            page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ProjectRequest> entityPage;

        if (status != null && !status.isBlank()) {
            try {
                ProjectStatusEnum statusEnum = ProjectStatusEnum.valueOf(status.toUpperCase().trim());
                entityPage = projectRequestService.getProjectRequestsByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(WsApiResponseDTO.error("Invalid status value: " + status));
            }
        } else {
            entityPage = projectRequestService.getAllProjectRequests(pageable);
        }

        Page<ProjectRequestResponseDTO> dtoPage = entityPage.map(projectRequestMapper::toAdminResponseDTO);

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Project requests retrieved successfully",
                        PaginatedResponseDTO.from(dtoPage)));
    }

    // ════════════════════════════════════════════════════════════════
    // ADMIN: Get single request
    // ════════════════════════════════════════════════════════════════

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<WsApiResponseDTO<ProjectRequestResponseDTO>> getProjectRequestById(
            @PathVariable Long id) {

        ProjectRequest entity = projectRequestService.getProjectRequestById(id);
        ProjectRequestResponseDTO responseDTO = projectRequestMapper.toAdminResponseDTO(entity);

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Project request retrieved successfully", responseDTO));
    }

    // ════════════════════════════════════════════════════════════════
    // ADMIN: Update status (WITH AUDIT)
    // ════════════════════════════════════════════════════════════════

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<WsApiResponseDTO<ProjectRequestResponseDTO>> updateProjectRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO statusUpdateDTO,
            Authentication authentication,
            HttpServletRequest request) {

        ProjectRequest existing = projectRequestService.getProjectRequestById(id);
        ProjectStatusEnum oldStatus = existing.getStatus();

        ProjectStatusEnum newStatus;
        try {
            newStatus = ProjectStatusEnum.valueOf(
                    statusUpdateDTO.getStatus().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    WsApiResponseDTO.error("Invalid status value: " + statusUpdateDTO.getStatus()));
        }

        ProjectRequest updatedEntity = projectRequestService.updateProjectRequestStatus(id, newStatus);

        // 🔎 AUDIT LOG
        auditTrailService.logStatusChange(
                authentication.getName(),
                id,
                oldStatus.name(),
                newStatus.name(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        ProjectRequestResponseDTO responseDTO = projectRequestMapper.toAdminResponseDTO(updatedEntity);

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Status updated successfully", responseDTO));
    }

    // ════════════════════════════════════════════════════════════════
    // SUPER_ADMIN: Soft delete (WITH AUDIT)
    // ════════════════════════════════════════════════════════════════

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<WsApiResponseDTO<Void>> softDeleteProjectRequest(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {

        projectRequestService.softDeleteProjectRequest(id);

        // 🔎 AUDIT LOG
        auditTrailService.logDelete(
                authentication.getName(),
                id,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Project request deleted successfully"));
    }
}