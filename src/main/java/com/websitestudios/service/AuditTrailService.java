package com.websitestudios.service;

import com.websitestudios.entity.AuditTrail;
import com.websitestudios.enums.AuditActionEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditTrailService {

    void logAction(Long adminId, AuditActionEnum action, String targetEntity,
            Long targetId, String oldValue, String newValue,
            String ipAddress, String userAgent);

    Page<AuditTrail> getAuditTrail(Pageable pageable);

    Page<AuditTrail> getAuditTrailByAdmin(Long adminId, Pageable pageable);
}