package com.websitestudios.service;

import com.websitestudios.entity.CountryCode;

import java.util.List;

/**
 * Service interface for country code operations.
 */
public interface CountryCodeService {

    /**
     * Get all country codes — cached via Redis.
     * Cache key: "country_codes:all"
     * TTL: 24 hours (data rarely changes)
     */
    List<CountryCode> getAllCountryCodes();

    /**
     * Find a country code by dial code (e.g., "+91").
     */
    CountryCode findByDialCode(String dialCode);

    /**
     * Evict the country codes cache (admin operation).
     */
    void evictCache();
}