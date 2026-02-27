package com.websitestudios.repository;

import com.websitestudios.entity.CountryCode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CountryCode entity.
 */
@Repository
public interface CountryCodeRepository extends JpaRepository<CountryCode, Long> {

    /**
     * Find all country codes ordered alphabetically by country name.
     */
    List<CountryCode> findAllByOrderByCountryNameAsc();

    /**
     * Find by dial code (e.g., "+91").
     */
    Optional<CountryCode> findByDialCode(String dialCode);

    /**
     * Find by ISO code (e.g., "IN").
     */
    Optional<CountryCode> findByIsoCode(String isoCode);
}