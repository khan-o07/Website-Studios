package com.websitestudios.security.sanitization;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * Input sanitizer to prevent XSS attacks.
 * Strips all HTML tags and dangerous content from user input.
 */
@Component
public class InputSanitizer {

    /**
     * Sanitize a string by removing all HTML tags and trimming.
     *
     * @param input Raw user input
     * @return Sanitized safe string
     */
    public String sanitize(String input) {
        if (input == null) {
            return null;
        }
        // Strip ALL HTML tags
        String cleaned = Jsoup.clean(input, Safelist.none());
        // Trim whitespace
        cleaned = cleaned.trim();
        // Normalize multiple spaces to single space
        cleaned = cleaned.replaceAll("\\s+", " ");
        return cleaned;
    }

    /**
     * Sanitize and lowercase (for emails).
     */
    public String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return sanitize(email).toLowerCase();
    }

    /**
     * Sanitize phone number — keep only digits.
     */
    public String sanitizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        return phone.replaceAll("[^0-9]", "");
    }

    /**
     * Sanitize country code — keep only + and digits.
     */
    public String sanitizeCountryCode(String code) {
        if (code == null) {
            return null;
        }
        return code.replaceAll("[^+0-9]", "");
    }
}