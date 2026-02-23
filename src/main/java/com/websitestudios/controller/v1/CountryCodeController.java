package com.websitestudios.controller.v1;

import com.websitestudios.dto.response.CountryCodeResponseDTO;
import com.websitestudios.dto.response.WsApiResponseDTO;
import com.websitestudios.entity.CountryCode;
import com.websitestudios.mapper.CountryCodeMapper;
import com.websitestudios.service.CountryCodeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public endpoint for country code dropdown data.
 * No authentication required.
 *
 * GET /api/v1/country-codes
 *
 * TODO Phase 7: Add caching (Redis) — this data rarely changes.
 */
@RestController
@RequestMapping("/api/v1/country-codes")
public class CountryCodeController {

    private static final Logger log = LoggerFactory.getLogger(CountryCodeController.class);

    private final CountryCodeService countryCodeService;
    private final CountryCodeMapper countryCodeMapper;

    public CountryCodeController(CountryCodeService countryCodeService,
            CountryCodeMapper countryCodeMapper) {
        this.countryCodeService = countryCodeService;
        this.countryCodeMapper = countryCodeMapper;
    }

    /**
     * GET /api/v1/country-codes
     * Returns all country codes for the frontend dropdown.
     *
     * Response: 200 OK
     * {
     * "success": true,
     * "message": "Country codes retrieved successfully",
     * "data": [
     * { "id": 1, "countryName": "India", "dialCode": "+91", "isoCode": "IN",
     * "flagEmoji": "🇮🇳" },
     * ...
     * ]
     * }
     */
    @GetMapping
    public ResponseEntity<WsApiResponseDTO<List<CountryCodeResponseDTO>>> getAllCountryCodes() {
        log.info("Fetching all country codes");

        List<CountryCode> countryCodes = countryCodeService.getAllCountryCodes();
        List<CountryCodeResponseDTO> responseDTOs = countryCodeMapper.toResponseDTOList(countryCodes);

        log.info("Returning {} country codes", responseDTOs.size());

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Country codes retrieved successfully", responseDTOs));
    }
}