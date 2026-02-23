package com.websitestudios.mapper;

import com.websitestudios.dto.response.CountryCodeResponseDTO;
import com.websitestudios.entity.CountryCode;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps between CountryCode entity and CountryCodeResponseDTO.
 * Simple 1:1 mapping — no sensitive data involved.
 */
@Component
public class CountryCodeMapper {

    /**
     * Convert single entity to response DTO.
     */
    public CountryCodeResponseDTO toResponseDTO(CountryCode entity) {
        if (entity == null) {
            return null;
        }
        return new CountryCodeResponseDTO(
                entity.getId(),
                entity.getCountryName(),
                entity.getDialCode(),
                entity.getIsoCode(),
                entity.getFlagEmoji());
    }

    /**
     * Convert list of entities to list of response DTOs.
     */
    public List<CountryCodeResponseDTO> toResponseDTOList(List<CountryCode> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}