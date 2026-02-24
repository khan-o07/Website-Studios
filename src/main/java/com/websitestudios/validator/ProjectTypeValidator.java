package com.websitestudios.validator;

import com.websitestudios.enums.ProjectTypeEnum;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates that all values in a List<String> are valid ProjectTypeEnum values.
 *
 * Valid: ["ANDROID_APP", "WEBSITE"]
 * Valid: ["android_app", "ios_app"] (case-insensitive)
 * Invalid: ["ANDROID_APP", "INVALID_SERVICE"]
 * Invalid: ["ANDROID_APP", "ANDROID_APP"] (duplicates rejected)
 */
public class ProjectTypeValidator implements ConstraintValidator<ValidProjectType, List<String>> {

    private static final Logger log = LoggerFactory.getLogger(ProjectTypeValidator.class);

    // Pre-compute allowed values for fast lookup
    private static final Set<String> ALLOWED_VALUES = Arrays.stream(ProjectTypeEnum.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    @Override
    public void initialize(ValidProjectType constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(List<String> values, ConstraintValidatorContext context) {
        // Null/empty check handled by @NotEmpty — let it pass here
        if (values == null || values.isEmpty()) {
            return true;
        }

        // Check for duplicates
        Set<String> uniqueValues = values.stream()
                .map(v -> v.toUpperCase().trim())
                .collect(Collectors.toSet());

        if (uniqueValues.size() != values.size()) {
            log.debug("Duplicate service types detected in: {}", values);
            customMessage(context, "Duplicate service types are not allowed.");
            return false;
        }

        // Check each value against the enum
        for (String value : values) {
            String upperValue = value.toUpperCase().trim();
            if (!ALLOWED_VALUES.contains(upperValue)) {
                log.debug("Invalid service type: '{}'. Allowed: {}", value, ALLOWED_VALUES);
                customMessage(context,
                        "Invalid service type: '" + value +
                                "'. Allowed values: " + String.join(", ", ALLOWED_VALUES));
                return false;
            }
        }

        return true;
    }

    /**
     * Replace the default message with a custom one.
     */
    private void customMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}