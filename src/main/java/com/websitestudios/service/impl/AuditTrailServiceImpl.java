package com.websitestudios.service.impl;

import com.websitestudios.entity.AuditTrail;
import com.websitestudios.entity.StudioAdmin;
import com.websitestudios.enums.AuditActionEnum;
import com.websitestudios.repository.AuditTrailRepository;
import com.websitestudios.repository.StudioAdminRepository;
import com.websitestudios.service.AuditTrailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditTrailServiceImpl implements AuditTrailService {

    private final AuditTrailRepository auditTrailRepository;
    private final StudioAdminRepository studioAdminRepository;

    @Override
    @Async
    @Transactional
    public void logAction(Long adminId, AuditActionEnum action, String targetEntity,
            Long targetId, String oldValue, String newValue,
            String ipAddress, String userAgent) {

        StudioAdmin admin = null;
        if (adminId != null) {
            admin = studioAdminRepository.findById(adminId).orElse(null);
        }

        AuditTrail auditTrail = AuditTrail.builder()
                .admin(admin)
                .action(action)
                .targetEntity(targetEntity)
                .targetId(targetId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress != null ? ipAddress : "unknown")
                .userAgent(userAgent)
                .build();

        auditTrailRepository.save(auditTrail);
        log.debug("Audit logged: action={}, target={}, targetId={}",
                action, targetEntity, targetId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditTrail> getAuditTrail(Pageable pageable) {
        return auditTrailRepository.findAllByOrderByPerformedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditTrail> getAuditTrailByAdmin(Long adminId, Pageable pageable) {
        return auditTrailRepository.findByAdminIdOrderByPerformedAtDesc(adminId, pageable);
    }
}