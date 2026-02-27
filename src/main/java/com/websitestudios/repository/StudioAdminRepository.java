package com.websitestudios.repository;

import com.websitestudios.entity.StudioAdmin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for StudioAdmin entity.
 * Used by WsUserDetailsService and AccountLockoutService.
 */
@Repository
public interface StudioAdminRepository extends JpaRepository<StudioAdmin, Long> {

    Optional<StudioAdmin> findByUsername(String username);

    Optional<StudioAdmin> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}