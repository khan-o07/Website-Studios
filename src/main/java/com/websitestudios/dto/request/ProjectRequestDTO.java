package com.websitestudios.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for public form submission — "Start a Project" form.
 *
 * Standard JSR-303 annotations are applied here.
 * Custom validators (@ValidPhoneNumber, @ValidProjectType) will be added in
 * Phase 4.
 */
public class ProjectRequestDTO {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
    private String fullName;

    @NotEmpty(message = "At least one service type must be selected")
    // TODO Phase 4: Add @ValidProjectType (custom validator to check enum values)
    private List<String> serviceTypes;

    @NotBlank(message = "Country code is required")
    @Pattern(regexp = "^\\+\\d{1,4}$", message = "Invalid country code format (e.g., +1, +91)")
    private String countryCode;

    @NotBlank(message = "Phone number is required")
    @Size(min = 6, max = 15, message = "Phone number must be between 6 and 15 digits")
    // TODO Phase 4: Add @ValidPhoneNumber (custom regex validator)
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "reCAPTCHA token is required")
    // TODO Phase 7: reCAPTCHA verification logic in service layer
    private String recaptchaToken;

    // ──────────────────────────── Constructors ────────────────────────────

    public ProjectRequestDTO() {
    }

    public ProjectRequestDTO(String fullName, List<String> serviceTypes,
            String countryCode, String phoneNumber,
            String email, String recaptchaToken) {
        this.fullName = fullName;
        this.serviceTypes = serviceTypes;
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.recaptchaToken = recaptchaToken;
    }

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<String> serviceTypes) {
        this.serviceTypes = serviceTypes;
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

    public String getRecaptchaToken() {
        return recaptchaToken;
    }

    public void setRecaptchaToken(String recaptchaToken) {
        this.recaptchaToken = recaptchaToken;
    }

    @Override
    public String toString() {
        // NEVER log sensitive fields — mask them
        return "ProjectRequestDTO{" +
                "fullName='" + fullName + '\'' +
                ", serviceTypes=" + serviceTypes +
                ", countryCode='" + countryCode + '\'' +
                ", phoneNumber='[MASKED]'" +
                ", email='[MASKED]'" +
                '}';
    }
}