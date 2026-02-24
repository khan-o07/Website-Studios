package com.websitestudios.service.impl;

import com.websitestudios.entity.ProjectRequest;
import com.websitestudios.enums.ProjectStatusEnum;
import com.websitestudios.exception.DuplicateRequestException;
import com.websitestudios.exception.InvalidInputException;
import com.websitestudios.exception.ResourceNotFoundException;
import com.websitestudios.repository.ProjectRequestRepository;
import com.websitestudios.security.encryption.AESEncryptionUtil;
import com.websitestudios.security.sanitization.InputSanitizer;
import com.websitestudios.service.ProjectRequestService;
import com.websitestudios.validator.EmailDomainValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Implementation of ProjectRequestService.
 *
 * Full submission flow:
 * Sanitize → Validate Domain → Duplicate Check → Encrypt → Hash → Save
 *
 * Now uses proper custom exceptions (Phase 4).
 *
 * TODO Phase 7: reCAPTCHA verification
 * TODO Phase 8: Audit trail logging on status changes
 */
@Service
@Transactional
public class ProjectRequestServiceImpl implements ProjectRequestService {

    private static final Logger log = LoggerFactory.getLogger(ProjectRequestServiceImpl.class);

    private final ProjectRequestRepository projectRequestRepository;
    private final AESEncryptionUtil encryptionUtil;
    private final InputSanitizer inputSanitizer;
    private final EmailDomainValidator emailDomainValidator;

    public ProjectRequestServiceImpl(ProjectRequestRepository projectRequestRepository,
            AESEncryptionUtil encryptionUtil,
            InputSanitizer inputSanitizer,
            EmailDomainValidator emailDomainValidator) {
        this.projectRequestRepository = projectRequestRepository;
        this.encryptionUtil = encryptionUtil;
        this.inputSanitizer = inputSanitizer;
        this.emailDomainValidator = emailDomainValidator;
    }

    // ════════════════════════════════════════════════════════════════
    // CREATE — Public form submission
    // ════════════════════════════════════════════════════════════════

    @Override
    public ProjectRequest createProjectRequest(ProjectRequest entity, String rawEmail,
            String rawPhone, String captchaToken) {

        log.info("Processing new project request submission");

        // Step 1: Sanitize inputs
        String sanitizedEmail = inputSanitizer.sanitize(rawEmail).toLowerCase().trim();
        String sanitizedPhone = inputSanitizer.sanitize(rawPhone).trim();
        String sanitizedName = inputSanitizer.sanitize(entity.getFullName()).trim();

        entity.setFullName(sanitizedName);

        // Step 2: Validate email domain (block disposable emails)
        if (!emailDomainValidator.isValidDomain(sanitizedEmail)) {
            log.warn("Disposable email domain blocked: {}",
                    emailDomainValidator.extractDomain(sanitizedEmail));
            throw new InvalidInputException("email",
                    "Disposable or temporary email addresses are not allowed. Please use a valid email.");
        }

        // Step 3: Generate hashes for duplicate check + indexing
        String emailHash = sha256Hash(sanitizedEmail);
        String phoneHash = sha256Hash(sanitizedPhone);

        // Step 4: Check for duplicate (same email AND phone, non-deleted)
        boolean isDuplicate = projectRequestRepository
                .existsByEmailHashAndPhoneHashAndIsDeletedFalse(emailHash, phoneHash);

        if (isDuplicate) {
            log.warn("Duplicate project request detected for email_hash: {} and phone_hash: {}",
                    emailHash.substring(0, 8) + "...",
                    phoneHash.substring(0, 8) + "...");
            throw new DuplicateRequestException(
                    "A project request with this email and phone number already exists. " +
                            "If you need to update your request, please contact us.");
        }

        // Step 5: Encrypt sensitive fields
        try {
            entity.setEmail(encryptionUtil.encrypt(sanitizedEmail));
            entity.setPhoneNumber(encryptionUtil.encrypt(sanitizedPhone));
        } catch (Exception e) {
            log.error("Encryption failed during project request creation", e);
            throw new RuntimeException("Failed to process your request. Please try again later.");
        }

        // Step 6: Set hashes for future lookups
        entity.setEmailHash(emailHash);
        entity.setPhoneHash(phoneHash);

        // Step 7: Set defaults
        entity.setStatus(ProjectStatusEnum.PENDING);
        entity.setIsDeleted(false);

        // Step 8: reCAPTCHA score (TODO Phase 7: actual verification)
        entity.setRecaptchaScore(null);

        // Step 9: Save
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

        return projectRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProjectRequest", "id", id));
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
    public Page<ProjectRequest> getProjectRequestsByStatus(ProjectStatusEnum status,
            Pageable pageable) {
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

    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Hashing failed — SHA-256 not available");
        }
    }
}