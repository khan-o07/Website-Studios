package com.websitestudios.repository;

import com.websitestudios.entity.CountryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CountryCode entity.
 * This is a READ-ONLY repository in production.
 */
@Repository
public interface CountryCodeRepository extends JpaRepository<CountryCode, Long> {

    /**
     * Find all country codes sorted by country name.
     */
    List<CountryCode> findAllByOrderByCountryNameAsc();

    /**
     * Find country by ISO code (unique).
     */
    Optional<CountryCode> findByIsoCode(String isoCode);

    /**
     * Find all countries with a specific dial code.
     * Multiple countries can share the same dial code (e.g., +1).
     */
    List<CountryCode> findByDialCode(String dialCode);

    /**
     * Check if a dial code exists.
     */
    boolean existsByDialCode(String dialCode);
}