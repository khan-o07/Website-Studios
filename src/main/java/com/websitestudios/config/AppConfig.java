package com.websitestudios.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * General application beans.
 * UPDATED in Phase 7: Added @EnableCaching for Redis cache support.
 */
@Configuration
@EnableCaching // ← NEW: Enables @Cacheable, @CacheEvict annotations
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public RestTemplate restTemplate() {
        log.info("Creating RestTemplate bean");
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        log.info("Creating custom ObjectMapper bean");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}