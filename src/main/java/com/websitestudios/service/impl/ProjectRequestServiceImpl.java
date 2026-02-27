package com.websitestudios.service.impl;

import com.websitestudios.captcha.RecaptchaService;
import com.websitestudios.entity.ProjectRequest;
import com.websitestudios.enums.ProjectStatusEnum;
import com.websitestudios.exception.DuplicateRequestException;
import com.websitestudios.exception.InvalidInputException;
import com.websitestudios.exception.ResourceNotFoundException;
import com.websitestudios.ratelimit.DuplicateRequestThrottler;
import com.websitestudios.repository.ProjectRequestRepository;
import com.websitestudios.security.encryption.AESEncryptionUtil;
import com.websitestudios.security.sanitization.InputSanitizer;
import com.websitestudios.service.ProjectRequestService;
import com.websitestudios.validator.EmailDomainValidator;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ProjectRequestServiceImpl — UPDATED in Phase 7.
 *
 * New additions:
 * - reCAPTCHA verification (Step 1)
 * - DuplicateRequestThrottler cooldown check (Step 3)
 */
@Service
@Transactional
public class ProjectRequestServiceImpl implements ProjectRequestService {

    private static final Logger log = LoggerFactory.getLogger(ProjectRequestServiceImpl.class);

    private final ProjectRequestRepository projectRequestRepository;
    private final AESEncryptionUtil encryptionUtil;
    private final InputSanitizer inputSanitizer;
    private final EmailDomainValidator emailDomainValidator;
    private final RecaptchaService recaptchaService;
    private final DuplicateRequestThrottler duplicateRequestThrottler;

    public ProjectRequestServiceImpl(
            ProjectRequestRepository projectRequestRepository,
            AESEncryptionUtil encryptionUtil,
            InputSanitizer inputSanitizer,
            EmailDomainValidator emailDomainValidator,
            RecaptchaService recaptchaService,
            DuplicateRequestThrottler duplicateRequestThrottler) {
        this.projectRequestRepository = projectRequestRepository;
        this.encryptionUtil = encryptionUtil;
        this.inputSanitizer = inputSanitizer;
        this.emailDomainValidator = emailDomainValidator;
        this.recaptchaService = recaptchaService;
        this.duplicateRequestThrottler = duplicateRequestThrottler;
    }

    // ════════════════════════════════════════════════════════════════
    // CREATE — Full Phase 7 Submission Flow
    // ════════════════════════════════════════════════════════════════

    @Override
    public ProjectRequest createProjectRequest(ProjectRequest entity, String rawEmail,
            String rawPhone, String captchaToken) {

        log.info("Processing new project request submission");

        // ── Step 1: reCAPTCHA Verification ──────────────────────────
        String clientIp = extractClientIp();
        double rawCaptchaScore = 0.0;
        BigDecimal captchaScore = BigDecimal.ZERO;

        try {
            var captchaResponse = recaptchaService.verify(captchaToken, clientIp);
            rawCaptchaScore = captchaResponse.getScore();
            captchaScore = BigDecimal.valueOf(rawCaptchaScore).setScale(2, RoundingMode.HALF_UP);
            log.info("reCAPTCHA passed — score: {}", captchaScore);
        } catch (Exception e) {
            log.warn("reCAPTCHA verification failed: {}", e.getMessage());
            throw e; // Re-throw — GlobalExceptionHandler handles it as 400
        }

        // ── Step 2: Sanitize inputs ──────────────────────────────────
        String sanitizedEmail = inputSanitizer.sanitize(rawEmail).toLowerCase().trim();
        String sanitizedPhone = inputSanitizer.sanitize(rawPhone).trim();
        String sanitizedName = inputSanitizer.sanitize(entity.getFullName()).trim();

        entity.setFullName(sanitizedName);

        // ── Step 3: Validate email domain ────────────────────────────
        if (!emailDomainValidator.isValidDomain(sanitizedEmail)) {
            log.warn("Blocked disposable email domain: {}",
                    emailDomainValidator.extractDomain(sanitizedEmail));
            throw new InvalidInputException("email",
                    "Disposable or temporary email addresses are not allowed.");
        }

        // ── Step 4: Generate hashes ──────────────────────────────────
        String emailHash = sha256Hash(sanitizedEmail);
        String phoneHash = sha256Hash(sanitizedPhone);

        // ── Step 5: Cooldown throttle check ─────────────────────────
        // Checks if same contact submitted within the last N minutes
        duplicateRequestThrottler.checkCooldown(emailHash, phoneHash);

        // ── Step 6: Hard duplicate check ─────────────────────────────
        // Checks if this exact combination has EVER been submitted (non-deleted)
        boolean isDuplicate = projectRequestRepository
                .existsByEmailHashAndPhoneHashAndIsDeletedFalse(emailHash, phoneHash);

        if (isDuplicate) {
            log.warn("Duplicate project request detected — email_hash: {}..., phone_hash: {}...",
                    emailHash.substring(0, 8), phoneHash.substring(0, 8));
            throw new DuplicateRequestException(
                    "A project request with this email and phone number already exists. " +
                            "If you need assistance, please contact us directly.");
        }

        // ── Step 7: Encrypt sensitive fields ─────────────────────────
        try {
            entity.setEmail(encryptionUtil.encrypt(sanitizedEmail));
            entity.setPhoneNumber(encryptionUtil.encrypt(sanitizedPhone));
        } catch (Exception e) {
            log.error("Encryption failed during project request creation", e);
            throw new RuntimeException("Failed to process your request. Please try again later.");
        }

        // ── Step 8: Set hashes and metadata ──────────────────────────
        entity.setEmailHash(emailHash);
        entity.setPhoneHash(phoneHash);
        entity.setStatus(ProjectStatusEnum.PENDING);
        entity.setIsDeleted(false);
        entity.setRecaptchaScore(captchaScore);

        // ── Step 9: Save to database ──────────────────────────────────
        ProjectRequest savedEntity = projectRequestRepository.save(entity);

        log.info("Project request saved — ID: {}, captcha score: {}",
                savedEntity.getId(), captchaScore);

        return savedEntity;
    }

    // ════════════════════════════════════════════════════════════════
    // READ, UPDATE, DELETE — Unchanged from Phase 4
    // ════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public ProjectRequest getProjectRequestById(Long id) {
        return projectRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectRequest", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectRequest> getAllProjectRequests(Pageable pageable) {
        return projectRequestRepository.findByIsDeletedFalse(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectRequest> getProjectRequestsByStatus(ProjectStatusEnum status, Pageable pageable) {
        return projectRequestRepository.findByStatusAndIsDeletedFalse(status, pageable);
    }

    @Override
    public ProjectRequest updateProjectRequestStatus(Long id, ProjectStatusEnum newStatus) {
        ProjectRequest entity = getProjectRequestById(id);
        ProjectStatusEnum oldStatus = entity.getStatus();
        entity.setStatus(newStatus);
        entity.setUpdatedAt(Instant.now());
        ProjectRequest updated = projectRequestRepository.save(entity);
        log.info("Project request ID: {} status: {} → {}", id, oldStatus, newStatus);
        return updated;
    }

    @Override
    public void softDeleteProjectRequest(Long id) {
        ProjectRequest entity = getProjectRequestById(id);
        entity.setIsDeleted(true);
        entity.setDeletedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        projectRequestRepository.save(entity);
        log.info("Project request ID: {} soft-deleted", id);
    }

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
            return 0;
        }
    }

    // ════════════════════════════════════════════════════════════════
    // UTILITIES
    // ════════════════════════════════════════════════════════════════

    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Extract client IP from the current request context.
     */
    private String extractClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String forwardedFor = request.getHeader("X-Forwarded-For");
                if (forwardedFor != null && !forwardedFor.isBlank()) {
                    return forwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not extract client IP from request context");
        }
        return "unknown";
    }
}