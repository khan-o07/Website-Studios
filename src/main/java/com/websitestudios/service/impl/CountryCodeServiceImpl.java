package com.websitestudios.service.impl;

import com.websitestudios.entity.CountryCode;
import com.websitestudios.repository.CountryCodeRepository;
import com.websitestudios.service.CountryCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryCodeServiceImpl implements CountryCodeService {

    private final CountryCodeRepository countryCodeRepository;

    @Override
    public List<CountryCode> getAllCountryCodes() {
        log.debug("Fetching all country codes");
        return countryCodeRepository.findAllByOrderByCountryNameAsc();
    }

    @Override
    public boolean isValidDialCode(String dialCode) {
        return countryCodeRepository.existsByDialCode(dialCode);
    }
}