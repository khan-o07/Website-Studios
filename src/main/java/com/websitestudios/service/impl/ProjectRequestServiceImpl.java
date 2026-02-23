package com.websitestudios.service.impl;

import com.websitestudios.entity.ProjectRequest;
import com.websitestudios.entity.ProjectServiceType;
import com.websitestudios.enums.ProjectStatusEnum;
import com.websitestudios.enums.ProjectTypeEnum;
import com.websitestudios.repository.ProjectRequestRepository;
import com.websitestudios.security.encryption.AESEncryptionUtil;
import com.websitestudios.security.sanitization.InputSanitizer;
import com.websitestudios.service.ProjectRequestService;
import com.websitestudios.util.WsAppUtils;
import com.websitestudios.util.WsConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectRequestServiceImpl implements ProjectRequestService {

    private final ProjectRequestRepository projectRequestRepository;
    private final AESEncryptionUtil aesEncryptionUtil;
    private final InputSanitizer inputSanitizer;

    @Override
    public ProjectRequest submitRequest(String fullName, String countryCode,
            String phoneNumber, String email,
            List<String> serviceTypes,
            BigDecimal recaptchaScore, String clientIp) {

        // Step 1: Sanitize all inputs
        String sanitizedName = inputSanitizer.sanitize(fullName);
        String sanitizedEmail = inputSanitizer.sanitizeEmail(email);
        String sanitizedPhone = inputSanitizer.sanitizePhone(phoneNumber);
        String sanitizedCountryCode = inputSanitizer.sanitizeCountryCode(countryCode);

        // Step 2: Generate hashes for duplicate detection
        String emailHash = WsAppUtils.sha256Hash(sanitizedEmail);
        String phoneHash = WsAppUtils.sha256Hash(sanitizedPhone);

        // Step 3: Check for duplicate request within last 10 minutes
        Instant tenMinutesAgo = Instant.now().minus(10, ChronoUnit.MINUTES);
        if (projectRequestRepository.existsRecentByEmailHash(emailHash, tenMinutesAgo)) {
            throw new RuntimeException(WsConstants.MSG_DUPLICATE_REQUEST);
        }

        // Step 4: Check for existing request with same email + phone
        if (projectRequestRepository.existsByEmailHashAndPhoneHash(emailHash, phoneHash)) {
            throw new RuntimeException(WsConstants.MSG_DUPLICATE_REQUEST);
        }

        // Step 5: Encrypt sensitive data
        String encryptedEmail = aesEncryptionUtil.encrypt(sanitizedEmail);
        String encryptedPhone = aesEncryptionUtil.encrypt(sanitizedPhone);

        // Step 6: Build entity
        ProjectRequest request = ProjectRequest.builder()
                .fullName(sanitizedName)
                .countryCode(sanitizedCountryCode)
                .phoneNumber(encryptedPhone)
                .email(encryptedEmail)
                .emailHash(emailHash)
                .phoneHash(phoneHash)
                .status(ProjectStatusEnum.PENDING)
                .isDeleted(false)
                .recaptchaScore(recaptchaScore)
                .clientIp(clientIp != null ? WsAppUtils.sha256Hash(clientIp) : null)
                .build();

        // Step 7: Add service types
        for (String type : serviceTypes) {
            ProjectTypeEnum projectType = ProjectTypeEnum.fromString(type);
            if (projectType != null) {
                ProjectServiceType serviceType = ProjectServiceType.builder()
                        .projectType(projectType)
                        .build();
                request.addServiceType(serviceType);
            }
        }

        // Step 8: Save
        ProjectRequest saved = projectRequestRepository.save(request);
        log.info("New project request submitted: id={}, name={}", saved.getId(),
                WsAppUtils.maskEmail(sanitizedEmail));

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectRequest getRequestById(Long id) {
        return projectRequestRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException(WsConstants.MSG_REQUEST_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectRequest> getAllRequests(Pageable pageable) {
        return projectRequestRepository.findAllActive(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectRequest> getRequestsByStatus(ProjectStatusEnum status, Pageable pageable) {
        return projectRequestRepository.findAllActiveByStatus(status, pageable);
    }

    @Override
    public ProjectRequest updateStatus(Long id, ProjectStatusEnum newStatus) {
        ProjectRequest request = getRequestById(id);
        request.setStatus(newStatus);
        ProjectRequest updated = projectRequestRepository.save(request);
        log.info("Project request status updated: id={}, newStatus={}", id, newStatus);
        return updated;
    }

    @Override
    public void softDeleteRequest(Long id) {
        ProjectRequest request = getRequestById(id);
        request.softDelete();
        projectRequestRepository.save(request);
        log.info("Project request soft deleted: id={}", id);
    }
}