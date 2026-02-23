package com.websitestudios.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * General utility methods for Website Studios.
 */
public final class WsAppUtils {

    private WsAppUtils() {
        throw new UnsupportedOperationException(
                "WsAppUtils is a utility class and cannot be instantiated");
    }

    /**
     * Generate SHA-256 hash of a string.
     * Used for creating email_hash and phone_hash for lookups.
     *
     * @param input The string to hash
     * @return Hex-encoded SHA-256 hash (64 characters)
     */
    public static String sha256Hash(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input for hashing cannot be null or blank");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                    input.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Mask an email address for safe display.
     * Example: "john.doe@example.com" → "jo***@example.com"
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }
        return email.replaceAll(
                WsConstants.EMAIL_MASK_PATTERN,
                WsConstants.EMAIL_MASK_REPLACEMENT);
    }

    /**
     * Mask a phone number for safe display.
     * Example: "9876543210" → "******3210"
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() <= WsConstants.PHONE_VISIBLE_LAST_DIGITS) {
            return "****";
        }
        int visibleStart = phone.length() - WsConstants.PHONE_VISIBLE_LAST_DIGITS;
        return "*".repeat(visibleStart) + phone.substring(visibleStart);
    }
}