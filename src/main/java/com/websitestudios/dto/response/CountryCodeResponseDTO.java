package com.websitestudios.dto.response;

/**
 * DTO for country code dropdown on the frontend.
 *
 * Example JSON:
 * {
 * "id": 1,
 * "countryName": "India",
 * "dialCode": "+91",
 * "isoCode": "IN",
 * "flagEmoji": "🇮🇳"
 * }
 */
public class CountryCodeResponseDTO {

    private Long id;
    private String countryName;
    private String dialCode;
    private String isoCode;
    private String flagEmoji;

    // ──────────────────────────── Constructors ────────────────────────────

    public CountryCodeResponseDTO() {
    }

    public CountryCodeResponseDTO(Long id, String countryName,
            String dialCode, String isoCode,
            String flagEmoji) {
        this.id = id;
        this.countryName = countryName;
        this.dialCode = dialCode;
        this.isoCode = isoCode;
        this.flagEmoji = flagEmoji;
    }

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getDialCode() {
        return dialCode;
    }

    public void setDialCode(String dialCode) {
        this.dialCode = dialCode;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getFlagEmoji() {
        return flagEmoji;
    }

    public void setFlagEmoji(String flagEmoji) {
        this.flagEmoji = flagEmoji;
    }
}