package com.websitestudios.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for phone numbers.
 *
 * Rules:
 * - Only digits allowed (no spaces, dashes, or special characters)
 * - Length: 6 to 15 digits
 * - No leading zeros (optional — depends on country)
 *
 * Usage:
 * 
 * @ValidPhoneNumber
 *                   private String phoneNumber;
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {

    String message() default "Invalid phone number. Must be 6-15 digits with no special characters.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}