package com.websitestudios.service.impl;

import com.websitestudios.entity.ProjectRequest;
import com.websitestudios.enums.ProjectStatusEnum;
import com.websitestudios.repository.ProjectRequestRepository;
import com.websitestudios.security.encryption.AESEncryptionUtil;
import com.websitestudios.security.sanitization.InputSanitizer;
import com.websitestudios.service.ProjectRequestService;

import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Implementation of ProjectRequestService.
 *
 * Handles the complete submission flow:
 * Sanitize → Duplicate Check → Encrypt → Hash → Save
 *
 * TODO Phase 4: Throw proper custom exceptions (ResourceNotFoundException,
 * DuplicateRequestException)
 * TODO Phase 7: reCAPTCHA verification
 * TODO Phase 8: Audit trail logging on status changes
 */
@Service
@Transactional
@Slf4j
public class ProjectRequestServiceImpl implements ProjectRequestService {

    private final ProjectRequestRepository projectRequestRepository;
    private final AESEncryptionUtil encryptionUtil;
    private final InputSanitizer inputSanitizer;

    public ProjectRequestServiceImpl(ProjectRequestRepository projectRequestRepository,
            AESEncryptionUtil encryptionUtil,
            InputSanitizer inputSanitizer) {
        this.projectRequestRepository = projectRequestRepository;
        this.encryptionUtil = encryptionUtil;
        this.inputSanitizer = inputSanitizer;
    }

    // ════════════════════════════════════════════════════════════════
    // CREATE — Public form submission
    // ════════════════════════════════════════════════════════════════

    @Override
    public ProjectRequest createProjectRequest(ProjectRequest entity, String rawEmail,
            String rawPhone, String captchaToken) {

        log.info("Processing new project request submission");

        // Step 1: Sanitize inputs (null-safe)
        String sanitizedEmail = inputSanitizer.sanitize(rawEmail);
        sanitizedEmail = sanitizedEmail == null ? "" : sanitizedEmail.toLowerCase().trim();

        String sanitizedPhone = inputSanitizer.sanitize(rawPhone);
        sanitizedPhone = sanitizedPhone == null ? "" : sanitizedPhone.trim();

        String sanitizedName = inputSanitizer.sanitize(entity.getFullName());
        sanitizedName = sanitizedName == null ? "" : sanitizedName.trim();

        entity.setFullName(sanitizedName);

        // Step 2: Generate hashes for duplicate check + indexing
        String emailHash = sha256Hash(sanitizedEmail);
        String phoneHash = sha256Hash(sanitizedPhone);

        // Step 3: Check for duplicate (same email AND phone, non-deleted)
        boolean isDuplicate = projectRequestRepository.existsByEmailHashAndPhoneHashAndIsDeletedFalse(
                emailHash, phoneHash);

        if (isDuplicate) {
            log.warn("Duplicate project request detected for email_hash: {} and phone_hash: {}",
                    abbreviateHash(emailHash),
                    abbreviateHash(phoneHash));
            // TODO Phase 4: throw new DuplicateRequestException("A request with this email
            // and phone already exists");
            throw new RuntimeException("Duplicate request: A request with this email and phone already exists");
        }

        // Step 4: Encrypt sensitive fields
        try {
            entity.setEmail(encryptionUtil.encrypt(sanitizedEmail));
            entity.setPhoneNumber(encryptionUtil.encrypt(sanitizedPhone));
        } catch (Exception e) {
            log.error("Encryption failed during project request creation", e);
            throw new RuntimeException("Failed to process request. Please try again.");
        }

        // Step 5: Set hashes for future lookups
        entity.setEmailHash(emailHash);
        entity.setPhoneHash(phoneHash);

        // Step 6: Set defaults
        entity.setStatus(ProjectStatusEnum.PENDING);
        entity.setIsDeleted(false);

        // Step 7: reCAPTCHA score (TODO Phase 7: actual verification)
        // entity.setRecaptchaScore(recaptchaService.verify(captchaToken).getScore());
        entity.setRecaptchaScore(null); // Placeholder

        // Step 8: Save
        ProjectRequest savedEntity = projectRequestRepository.save(entity);

        log.info("Project request saved successfully with ID: {}", savedEntity.getId());

        return savedEntity;
    }

    // ════════════════════════════════════════════════════════════════
    // READ — Single by ID
    // ════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public ProjectRequest getProjectRequestById(Long id) {
        log.info("Fetching project request with ID: {}", id);

        // TODO Phase 4: throw new ResourceNotFoundException("Project request not found
        // with ID: " + id);
        return projectRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Project request not found with ID: " + id));
    }

    // ════════════════════════════════════════════════════════════════
    // READ — All (paginated)
    // ════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectRequest> getAllProjectRequests(Pageable pageable) {
        log.info("Fetching all project requests - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return projectRequestRepository.findByIsDeletedFalse(pageable);
    }

    // ════════════════════════════════════════════════════════════════
    // READ — Filtered by status (paginated)
    // ════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectRequest> getProjectRequestsByStatus(ProjectStatusEnum status, Pageable pageable) {
        log.info("Fetching project requests with status: {} - page: {}, size: {}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        return projectRequestRepository.findByStatusAndIsDeletedFalse(status, pageable);
    }

    // ════════════════════════════════════════════════════════════════
    // UPDATE — Status change
    // ════════════════════════════════════════════════════════════════

    @Override
    public ProjectRequest updateProjectRequestStatus(Long id, ProjectStatusEnum newStatus) {
        log.info("Updating status for project request ID: {} to {}", id, newStatus);

        ProjectRequest entity = getProjectRequestById(id);

        ProjectStatusEnum oldStatus = entity.getStatus();
        entity.setStatus(newStatus);
        entity.setUpdatedAt(Instant.now());

        ProjectRequest updatedEntity = projectRequestRepository.save(entity);

        log.info("Project request ID: {} status changed from {} to {}",
                id, oldStatus, newStatus);

        // TODO Phase 8: auditTrailService.logStatusChange(adminId, id, oldStatus,
        // newStatus);

        return updatedEntity;
    }

    // ════════════════════════════════════════════════════════════════
    // DELETE — Soft delete
    // ════════════════════════════════════════════════════════════════

    @Override
    public void softDeleteProjectRequest(Long id) {
        log.info("Soft-deleting project request ID: {}", id);

        ProjectRequest entity = getProjectRequestById(id);

        entity.setIsDeleted(true);
        entity.setDeletedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        projectRequestRepository.save(entity);

        log.info("Project request ID: {} soft-deleted", id);

        // TODO Phase 8: auditTrailService.logDeletion(adminId, id);
    }

    // ════════════════════════════════════════════════════════════════
    // COUNT — Dashboard stats
    // ════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return projectRequestRepository.countByIsDeletedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public long getCountByStatus(String status) {
        if (status == null) {
            log.warn("Null status provided for count query");
            return 0;
        }

        try {
            ProjectStatusEnum statusEnum = ProjectStatusEnum.valueOf(status.toUpperCase().trim());
            return projectRequestRepository.countByStatusAndIsDeletedFalse(statusEnum);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status for count query: {}", status);
            return 0;
        }
    }

    // ════════════════════════════════════════════════════════════════
    // UTILITY — SHA-256 Hashing
    // ════════════════════════════════════════════════════════════════

    /**
     * Generate SHA-256 hash of a string.
     * Used for duplicate detection on encrypted fields.
     */
    private String sha256Hash(String input) {
        try {
            if (input == null) {
                input = "";
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Hashing failed — SHA-256 not available");
        }
    }

    /**
     * Abbreviate a hex hash safely for logs.
     */
    private String abbreviateHash(String hash) {
        if (hash == null)
            return "null";
        return hash.length() > 8 ? hash.substring(0, 8) + "..." : hash;
    }
}