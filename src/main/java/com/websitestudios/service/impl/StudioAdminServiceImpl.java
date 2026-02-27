package com.websitestudios.service.impl;

import com.websitestudios.entity.StudioAdmin;
import com.websitestudios.repository.StudioAdminRepository;
import com.websitestudios.service.StudioAdminService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of StudioAdminService.
 */
@Service
@Transactional(readOnly = true)
public class StudioAdminServiceImpl implements StudioAdminService {

    private static final Logger log = LoggerFactory.getLogger(StudioAdminServiceImpl.class);

    private final StudioAdminRepository studioAdminRepository;

    public StudioAdminServiceImpl(StudioAdminRepository studioAdminRepository) {
        this.studioAdminRepository = studioAdminRepository;
    }

    @Override
    public Optional<StudioAdmin> findByUsername(String username) {
        log.debug("Finding admin by username: {}", username);
        return studioAdminRepository.findByUsername(username);
    }

    @Override
    public Optional<StudioAdmin> findByEmail(String email) {
        log.debug("Finding admin by email");
        return studioAdminRepository.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return studioAdminRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return studioAdminRepository.existsByEmail(email);
    }
}