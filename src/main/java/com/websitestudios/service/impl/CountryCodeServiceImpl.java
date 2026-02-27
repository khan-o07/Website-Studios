package com.websitestudios.service.impl;

import com.websitestudios.entity.CountryCode;
import com.websitestudios.exception.ResourceNotFoundException;
import com.websitestudios.repository.CountryCodeRepository;
import com.websitestudios.service.CountryCodeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CountryCode service implementation with Redis caching.
 *
 * Country code data is static — it changes very rarely.
 * We cache it in Redis to avoid repeated DB queries on every frontend load.
 *
 * Cache key: "country_codes:all"
 * TTL configured in application.yml under spring.cache
 */
@Service
@Transactional(readOnly = true)
public class CountryCodeServiceImpl implements CountryCodeService {

    private static final Logger log = LoggerFactory.getLogger(CountryCodeServiceImpl.class);

    private static final String CACHE_NAME = "country_codes";

    private final CountryCodeRepository countryCodeRepository;

    public CountryCodeServiceImpl(CountryCodeRepository countryCodeRepository) {
        this.countryCodeRepository = countryCodeRepository;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'all'")
    public List<CountryCode> getAllCountryCodes() {
        log.info("Fetching all country codes from DB (cache miss)");
        List<CountryCode> codes = countryCodeRepository.findAllByOrderByCountryNameAsc();
        log.info("Fetched {} country codes from DB", codes.size());
        return codes;
    }

    @Override
    public CountryCode findByDialCode(String dialCode) {
        log.debug("Finding country code by dial code: {}", dialCode);
        return countryCodeRepository.findByDialCode(dialCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CountryCode", "dialCode", dialCode));
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void evictCache() {
        log.info("Country codes cache evicted");
    }
}