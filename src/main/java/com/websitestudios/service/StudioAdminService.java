package com.websitestudios.service;

import com.websitestudios.entity.StudioAdmin;

import java.util.Optional;

/**
 * Service interface for studio admin operations.
 */
public interface StudioAdminService {

    Optional<StudioAdmin> findByUsername(String username);

    Optional<StudioAdmin> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}