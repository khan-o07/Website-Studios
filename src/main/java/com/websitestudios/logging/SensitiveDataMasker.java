package com.websitestudios.logging;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Masks sensitive data before it reaches log outputs.
 *
 * Used to sanitize log messages that may accidentally contain PII.
 *
 * Masks:
 * Email: john.doe@example.com → j***@e***.com
 * Phone: +919876543210 → +91*****210
 * Credit: 4111111111111111 → 4***********111
 * JWT: eyJhbGci... → eyJ***[MASKED]
 * IP: 192.168.1.100 → 192.168.1.0
 */
@Component
public class SensitiveDataMasker {

    // Regex patterns for detection
    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("\\b[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}\\b");

    private static final Pattern JWT_PATTERN = Pattern
            .compile("eyJ[A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+");

    private static final Pattern CREDIT_CARD_PATTERN = Pattern
            .compile("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13})\\b");

    // ════════════════════════════════════════════════════════════════
    // INDIVIDUAL MASKING METHODS
    // ════════════════════════════════════════════════════════════════

    /**
     * Mask an email address.
     * john.doe@example.com → j***@e***.com
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@"))
            return "***@***.***";

        try {
            String[] parts = email.split("@");
            String local = parts[0].charAt(0) + "***";
            String[] domain = parts[1].split("\\.");
            String maskedDomain = domain[0].charAt(0) + "***";
            String tld = domain.length > 1 ? domain[domain.length - 1] : "***";
            return local + "@" + maskedDomain + "." + tld;
        } catch (Exception e) {
            return "***@***.***";
        }
    }

    /**
     * Mask a phone number.
     * +919876543210 → +91*****210
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 4)
            return "****";
        String lastThree = phone.substring(phone.length() - 3);
        String prefix = phone.length() > 6 ? phone.substring(0, 3) : "";
        String masked = "*".repeat(Math.max(0, phone.length() - 6));
        return prefix + masked + lastThree;
    }

    /**
     * Mask a JWT token — keep header only.
     * eyJhbGci... → eyJ***[MASKED]
     */
    public String maskJwt(String jwt) {
        if (jwt == null || jwt.length() < 10)
            return "[MASKED_TOKEN]";
        return jwt.substring(0, 6) + "***[MASKED]";
    }

    /**
     * Mask all sensitive patterns in a log message string.
     * Use this for any log message that might contain PII.
     */
    public String maskAll(String message) {
        if (message == null)
            return null;

        String masked = message;

        // Mask JWTs first (before email might be extracted from claims)
        masked = JWT_PATTERN.matcher(masked).replaceAll("eyJ***[MASKED]");

        // Mask emails
        masked = EMAIL_PATTERN.matcher(masked).replaceAll(match -> {
            String[] parts = match.group().split("@");
            if (parts.length == 2) {
                return parts[0].charAt(0) + "***@***." +
                        (parts[1].contains(".") ? parts[1].substring(parts[1].lastIndexOf('.') + 1) : "***");
            }
            return "***@***.***";
        });

        // Mask credit cards
        masked = CREDIT_CARD_PATTERN.matcher(masked).replaceAll(match -> {
            String cc = match.group();
            return cc.substring(0, 4) + "***" + cc.substring(cc.length() - 3);
        });

        return masked;
    }

    /**
     * Anonymize an IP address for GDPR compliance.
     * 192.168.1.100 → 192.168.1.0
     */
    public String anonymizeIp(String ip) {
        if (ip == null)
            return null;
        if (ip.contains(":")) {
            // IPv6
            int lastColon = ip.lastIndexOf(':');
            return lastColon > 0 ? ip.substring(0, lastColon) + ":0" : ip;
        }
        // IPv4
        int lastDot = ip.lastIndexOf('.');
        return lastDot > 0 ? ip.substring(0, lastDot) + ".0" : ip;
    }
}