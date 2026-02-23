package com.websitestudios.repository;

import com.websitestudios.entity.StudioRole;
import com.websitestudios.enums.StudioRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for StudioRole entity.
 */
@Repository
public interface StudioRoleRepository extends JpaRepository<StudioRole, Long> {

    Optional<StudioRole> findByRoleName(StudioRoleEnum roleName);
}