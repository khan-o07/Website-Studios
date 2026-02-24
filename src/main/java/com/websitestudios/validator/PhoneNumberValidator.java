package com.websitestudios.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Validates phone numbers against strict rules.
 *
 * Rules:
 * - Must be digits only: [0-9]
 * - Length: 6 to 15 characters
 * - No whitespace, no dashes, no parentheses, no plus sign
 * (the + is part of country_code, not phone_number)
 *
 * Valid: "9876543210", "123456", "123456789012345"
 * Invalid: "+91123456", "98765-43210", "phone: 123", "", null
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private static final Logger log = LoggerFactory.getLogger(PhoneNumberValidator.class);

    // Only digits, 6-15 characters
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{6,15}$");

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null/blank check is handled by @NotBlank — let it pass here
        if (value == null || value.isBlank()) {
            return true;
        }

        String trimmed = value.trim();

        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            log.debug("Phone validation failed for value length: {}", trimmed.length());
            return false;
        }

        return true;
    }
}