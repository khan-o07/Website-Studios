package com.websitestudios.service;

import com.websitestudios.entity.CountryCode;

import java.util.List;

public interface CountryCodeService {

    List<CountryCode> getAllCountryCodes();

    boolean isValidDialCode(String dialCode);
}