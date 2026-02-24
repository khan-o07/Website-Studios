package com.websitestudios.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for project service type lists.
 *
 * Ensures every value in the list is a valid ProjectTypeEnum value.
 *
 * Valid values: ANDROID_APP, IOS_APP, WEBSITE, WEB_APPLICATION
 *
 * Usage:
 * 
 * @ValidProjectType
 *                   private List<String> serviceTypes;
 */
@Documented
@Constraint(validatedBy = ProjectTypeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProjectType {

    String message() default "Invalid service type. Allowed values: ANDROID_APP, IOS_APP, WEBSITE, WEB_APPLICATION";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}