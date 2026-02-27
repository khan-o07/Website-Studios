package com.websitestudios.captcha;

import com.websitestudios.exception.CaptchaVerificationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Google reCAPTCHA v3 verification service.
 *
 * Flow:
 * 1. Frontend receives a reCAPTCHA token after user interaction
 * 2. Frontend sends token in the form submission body
 * 3. This service sends the token to Google's API for verification
 * 4. Google returns a score: 0.0 (bot) → 1.0 (human)
 * 5. If score < minScore (default 0.5) → reject the submission
 *
 * IMPORTANT:
 * - reCAPTCHA verification is DISABLED in dev mode (captcha.enabled=false)
 * - This allows easy testing without a real reCAPTCHA token
 * - In production (captcha.enabled=true), verification is enforced
 */
@Service
public class RecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaService.class);

    private final RecaptchaProperties recaptchaProperties;
    private final RestTemplate restTemplate;

    public RecaptchaService(RecaptchaProperties recaptchaProperties,
            RestTemplate restTemplate) {
        this.recaptchaProperties = recaptchaProperties;
        this.restTemplate = restTemplate;

        log.info("reCAPTCHA Service initialized — enabled: {}, min score: {}",
                recaptchaProperties.isEnabled(),
                recaptchaProperties.getMinScore());
    }

    // ════════════════════════════════════════════════════════════════
    // VERIFY TOKEN
    // ════════════════════════════════════════════════════════════════

    /**
     * Verify a reCAPTCHA token with Google's API.
     *
     * @param token    The reCAPTCHA token from the frontend
     * @param clientIp The client's IP address
     * @return The verification response with score
     * @throws CaptchaVerificationException if verification fails or score is too
     *                                      low
     */
    public RecaptchaResponseDTO verify(String token, String clientIp) {

        // Skip verification if disabled (dev mode)
        if (!recaptchaProperties.isEnabled()) {
            log.debug("reCAPTCHA verification is DISABLED — passing through");
            RecaptchaResponseDTO mockResponse = new RecaptchaResponseDTO();
            mockResponse.setSuccess(true);
            mockResponse.setScore(1.0);
            mockResponse.setAction("submit");
            return mockResponse;
        }

        // Validate token is not empty
        if (token == null || token.isBlank()) {
            log.warn("Empty reCAPTCHA token received from IP: {}", clientIp);
            throw new CaptchaVerificationException("reCAPTCHA token is missing or empty.");
        }

        // Call Google API
        RecaptchaResponseDTO response = callGoogleApi(token, clientIp);

        // Check success flag
        if (!response.isSuccess()) {
            log.warn("reCAPTCHA verification failed from IP: {} — error codes: {}",
                    clientIp, response.getErrorCodes());
            throw new CaptchaVerificationException(
                    "reCAPTCHA verification failed. Please try again.",
                    response.getScore());
        }

        // Check score threshold
        if (response.getScore() < recaptchaProperties.getMinScore()) {
            log.warn("reCAPTCHA score too low from IP: {} — score: {} (min: {})",
                    clientIp, response.getScore(), recaptchaProperties.getMinScore());
            throw new CaptchaVerificationException(
                    "reCAPTCHA verification failed. Suspicious activity detected.",
                    response.getScore());
        }

        log.info("reCAPTCHA verification passed from IP: {} — score: {}", clientIp, response.getScore());

        return response;
    }

    /**
     * Verify and return just the score (convenience method).
     */
    public double getScore(String token, String clientIp) {
        return verify(token, clientIp).getScore();
    }

    // ════════════════════════════════════════════════════════════════
    // GOOGLE API CALL
    // ════════════════════════════════════════════════════════════════

    /**
     * Make HTTP POST request to Google reCAPTCHA verification API.
     */
    private RecaptchaResponseDTO callGoogleApi(String token, String clientIp) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", recaptchaProperties.getSecretKey());
            params.add("response", token);
            if (clientIp != null && !clientIp.isBlank()) {
                params.add("remoteip", clientIp);
            }

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            ResponseEntity<RecaptchaResponseDTO> responseEntity = restTemplate.postForEntity(
                    recaptchaProperties.getVerifyUrl(),
                    requestEntity,
                    RecaptchaResponseDTO.class);

            if (responseEntity.getBody() == null) {
                log.error("Empty response from Google reCAPTCHA API");
                throw new CaptchaVerificationException(
                        "reCAPTCHA verification service returned an empty response.");
            }

            log.debug("Google reCAPTCHA API response: {}", responseEntity.getBody());

            return responseEntity.getBody();

        } catch (RestClientException e) {
            log.error("Failed to call Google reCAPTCHA API: {}", e.getMessage());
            // Don't block the user if Google's API is down — fail open in this case
            // You may choose to fail closed depending on your security requirements
            log.warn("reCAPTCHA API unavailable — allowing submission (fail-open policy)");

            RecaptchaResponseDTO failOpenResponse = new RecaptchaResponseDTO();
            failOpenResponse.setSuccess(true);
            failOpenResponse.setScore(0.5); // Neutral score
            return failOpenResponse;
        }
    }
}