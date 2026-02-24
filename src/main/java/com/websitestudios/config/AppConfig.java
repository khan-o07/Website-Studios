package com.websitestudios.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * General application beans and configurations.
 *
 * Beans defined here:
 * - RestTemplate: For external API calls (reCAPTCHA in Phase 7)
 * - ObjectMapper: Customized JSON serialization
 */
@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    /**
     * RestTemplate for making external HTTP calls.
     * Used by: RecaptchaService (Phase 7)
     *
     * In production, consider using WebClient (reactive) instead.
     * RestTemplate is simpler and sufficient for our use case.
     */
    @Bean
    public RestTemplate restTemplate() {
        log.info("Creating RestTemplate bean");
        return new RestTemplate();
    }

    /**
     * Customized Jackson ObjectMapper.
     *
     * Configuration:
     * - Java 8 date/time support (LocalDateTime serialization)
     * - Disable writing dates as timestamps (use ISO-8601 strings)
     */
    @Bean
    public ObjectMapper objectMapper() {
        log.info("Creating custom ObjectMapper bean");

        ObjectMapper mapper = new ObjectMapper();

        // Support Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());

        // Write dates as ISO-8601 strings, not timestamps
        // "2025-01-15T10:30:00" instead of [2025, 1, 15, 10, 30, 0]
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}