package com.websitestudios.mapper;

import com.websitestudios.dto.response.AuditTrailResponseDTO;
import com.websitestudios.entity.AuditTrail;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps AuditTrail entity to AuditTrailResponseDTO.
 * Never exposes internal IDs or stack traces.
 */
@Component
public class AuditTrailMapper {

    public AuditTrailResponseDTO toResponseDTO(AuditTrail entity) {
        if (entity == null)
            return null;

        AuditTrailResponseDTO dto = new AuditTrailResponseDTO();
        dto.setId(entity.getId());
        dto.setAction(entity.getAction() != null ? entity.getAction().name() : null);
        dto.setTargetEntity(entity.getTargetEntity());
        dto.setTargetId(entity.getTargetId());
        dto.setOldValue(entity.getOldValue());
        dto.setNewValue(entity.getNewValue());
        dto.setIpAddress(entity.getIpAddress());
        dto.setPerformedAt(entity.getPerformedAt());

        // Include admin username if present
        if (entity.getAdminUser() != null) {
            dto.setAdminUsername(entity.getAdminUser().getUsername());
        }

        return dto;
    }

    public List<AuditTrailResponseDTO> toResponseDTOList(List<AuditTrail> entities) {
        if (entities == null)
            return List.of();
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}