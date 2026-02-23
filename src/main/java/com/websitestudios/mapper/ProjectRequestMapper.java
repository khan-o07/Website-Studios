package com.websitestudios.mapper;

import com.websitestudios.dto.request.ProjectRequestDTO;
import com.websitestudios.dto.response.ProjectRequestResponseDTO;
import com.websitestudios.entity.ProjectRequest;
import com.websitestudios.entity.ProjectServiceType;
import com.websitestudios.enums.ProjectStatusEnum;
import com.websitestudios.enums.ProjectTypeEnum;
import com.websitestudios.security.encryption.AESEncryptionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps between ProjectRequest entity and DTOs.
 *
 * Handles:
 * - DTO → Entity conversion (for creation)
 * - Entity → Public Response DTO (masked sensitive fields)
 * - Entity → Admin Response DTO (decrypted sensitive fields)
 */
@Component
public class ProjectRequestMapper {

    private static final Logger log = LoggerFactory.getLogger(ProjectRequestMapper.class);

    private final AESEncryptionUtil encryptionUtil;

    public ProjectRequestMapper(AESEncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }

    // ════════════════════════════════════════════════════════════════
    // DTO → Entity (for new submissions)
    // ════════════════════════════════════════════════════════════════

    /**
     * Convert ProjectRequestDTO to ProjectRequest entity.
     * NOTE: Encryption of email/phone and hash generation
     * should be handled in the SERVICE layer, not here.
     * This method maps the structural fields only.
     */
    public ProjectRequest toEntity(ProjectRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        ProjectRequest entity = new ProjectRequest();
        entity.setFullName(dto.getFullName() == null ? "" : dto.getFullName().trim());
        entity.setCountryCode(dto.getCountryCode() == null ? "" : dto.getCountryCode().trim());
        entity.setStatus(ProjectStatusEnum.PENDING);

        // Service types: String list → ProjectServiceType entities
        if (dto.getServiceTypes() != null) {
            List<ProjectServiceType> serviceTypes = dto.getServiceTypes().stream()
                    .filter(t -> t != null)
                    .map(type -> {
                        try {
                            ProjectServiceType serviceType = new ProjectServiceType();
                            serviceType.setProjectType(ProjectTypeEnum.valueOf(type.toUpperCase().trim()));
                            serviceType.setProjectRequest(entity);
                            return serviceType;
                        } catch (IllegalArgumentException ex) {
                            log.warn("Unknown project service type skipped: {}", type);
                            return null;
                        }
                    })
                    .filter(st -> st != null)
                    .collect(Collectors.toList());
            entity.setServiceTypes(serviceTypes);
        }

        return entity;
    }

    // ════════════════════════════════════════════════════════════════
    // Entity → Public Response DTO (MASKED — for end users)
    // ════════════════════════════════════════════════════════════════

    /**
     * Convert entity to response DTO with MASKED sensitive fields.
     * Used for: 201 Created response after public form submission.
     */
    public ProjectRequestResponseDTO toPublicResponseDTO(ProjectRequest entity) {
        if (entity == null) {
            return null;
        }

        ProjectRequestResponseDTO dto = new ProjectRequestResponseDTO();
        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setCountryCode(entity.getCountryCode());
        dto.setStatus(entity.getStatus().name());
        dto.setCreatedAt(
                entity.getCreatedAt() == null ? null : LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC));
        dto.setUpdatedAt(
                entity.getUpdatedAt() == null ? null : LocalDateTime.ofInstant(entity.getUpdatedAt(), ZoneOffset.UTC));

        // Mask sensitive fields
        dto.setEmail(maskEmail(decryptSafe(entity.getEmail())));
        dto.setPhoneNumber(maskPhone(decryptSafe(entity.getPhoneNumber())));

        // Map service types
        dto.setServiceTypes(extractServiceTypeNames(entity));

        return dto;
    }

    // ════════════════════════════════════════════════════════════════
    // Entity → Admin Response DTO (FULL — for admin panel)
    // ════════════════════════════════════════════════════════════════

    /**
     * Convert entity to response DTO with FULL decrypted data.
     * Used for: Admin list view, admin detail view.
     */
    public ProjectRequestResponseDTO toAdminResponseDTO(ProjectRequest entity) {
        if (entity == null) {
            return null;
        }

        ProjectRequestResponseDTO dto = new ProjectRequestResponseDTO();
        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setCountryCode(entity.getCountryCode());
        dto.setStatus(entity.getStatus().name());
        BigDecimal score = entity.getRecaptchaScore();
        dto.setRecaptchaScore(score == null ? null : score.doubleValue());
        dto.setCreatedAt(
                entity.getCreatedAt() == null ? null : LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC));
        dto.setUpdatedAt(
                entity.getUpdatedAt() == null ? null : LocalDateTime.ofInstant(entity.getUpdatedAt(), ZoneOffset.UTC));

        // Decrypt sensitive fields for admin view
        dto.setEmail(decryptSafe(entity.getEmail()));
        dto.setPhoneNumber(decryptSafe(entity.getPhoneNumber()));

        // Map service types
        dto.setServiceTypes(extractServiceTypeNames(entity));

        return dto;
    }

    // ════════════════════════════════════════════════════════════════
    // Helper Methods
    // ════════════════════════════════════════════════════════════════

    /**
     * Extract service type enum names from entity's child collection.
     */
    private List<String> extractServiceTypeNames(ProjectRequest entity) {
        if (entity.getServiceTypes() == null || entity.getServiceTypes().isEmpty()) {
            return Collections.emptyList();
        }
        return entity.getServiceTypes().stream()
                .map(st -> st.getProjectType().name())
                .collect(Collectors.toList());
    }

    /**
     * Safely decrypt a value. If decryption fails (e.g., already plain text
     * in dev), return the original value.
     */
    private String decryptSafe(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return encryptedValue;
        }
        try {
            return encryptionUtil.decrypt(encryptedValue);
        } catch (Exception e) {
            log.warn("Decryption failed, returning raw value. Likely unencrypted data in dev mode.");
            return encryptedValue;
        }
    }

    /**
     * Mask email: john.doe@example.com → j***@e***.com
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }
        try {
            String[] parts = email.split("@");
            String localPart = parts[0];
            String domainPart = parts[1];

            String maskedLocal = localPart.charAt(0) + "***";

            String[] domainParts = domainPart.split("\\.");
            String maskedDomain = domainParts[0].charAt(0) + "***";
            String tld = domainParts.length > 1 ? domainParts[domainParts.length - 1] : "***";

            return maskedLocal + "@" + maskedDomain + "." + tld;
        } catch (Exception e) {
            return "***@***.***";
        }
    }

    /**
     * Mask phone: 1234567890 → ******7890
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        String lastFour = phone.substring(phone.length() - 4);
        String masked = "*".repeat(phone.length() - 4);
        return masked + lastFour;
    }
}