package com.websitestudios.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for project request responses.
 *
 * Two usage modes:
 * 1. PUBLIC (after submission) → email & phone are MASKED
 * 2. ADMIN (list/detail view) → email & phone are FULL (decrypted)
 *
 * Example (public):
 * {
 * "id": 42,
 * "fullName": "John Doe",
 * "email": "j***@e***.com",
 * "phoneNumber": "******7890",
 * "serviceTypes": ["ANDROID_APP", "WEBSITE"],
 * "status": "PENDING",
 * "createdAt": "2025-01-15T10:30:00"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectRequestResponseDTO {

    private Long id;
    private String fullName;
    private String countryCode;
    private String phoneNumber;
    private String email;
    private List<String> serviceTypes;
    private String status;
    private Double recaptchaScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ──────────────────────────── Constructors ────────────────────────────

    public ProjectRequestResponseDTO() {
    }

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<String> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getRecaptchaScore() {
        return recaptchaScore;
    }

    public void setRecaptchaScore(Double recaptchaScore) {
        this.recaptchaScore = recaptchaScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ProjectRequestResponseDTO{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", status='" + status + '\'' +
                ", serviceTypes=" + serviceTypes +
                ", createdAt=" + createdAt +
                '}';
    }
}