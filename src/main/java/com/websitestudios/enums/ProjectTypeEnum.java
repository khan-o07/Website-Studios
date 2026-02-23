package com.websitestudios.enums;

/**
 * Represents the types of projects Website Studios offers.
 * Used when clients select which service(s) they need.
 *
 * Three core offerings:
 * - ANDROID_APP → Native/Cross-platform Android applications
 * - IOS_APP → Native/Cross-platform iOS applications
 * - WEBSITE → Websites, Web Applications, SPAs, PWAs
 * (covers all browser-based digital products)
 */
public enum ProjectTypeEnum {

    ANDROID_APP("Android Application"),
    IOS_APP("iOS Application"),
    WEBSITE("Website / Web Application");

    private final String displayName;

    ProjectTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Safely converts a string to ProjectTypeEnum.
     * Returns null if the value doesn't match any enum constant.
     *
     * @param value The string value to convert
     * @return The matching ProjectTypeEnum or null
     */
    public static ProjectTypeEnum fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ProjectTypeEnum.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Checks if a given string is a valid ProjectTypeEnum value.
     *
     * @param value The string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String value) {
        return fromString(value) != null;
    }

    /**
     * Returns all valid enum values as a comma-separated string.
     * Useful for error messages.
     *
     * @return String like "ANDROID_APP, IOS_APP, WEBSITE"
     */
    public static String getAllValidValues() {
        StringBuilder sb = new StringBuilder();
        for (ProjectTypeEnum type : values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(type.name());
        }
        return sb.toString();
    }
}