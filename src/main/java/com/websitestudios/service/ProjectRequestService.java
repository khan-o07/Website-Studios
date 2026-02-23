package com.websitestudios.service;

import com.websitestudios.entity.ProjectRequest;
import com.websitestudios.enums.ProjectStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectRequestService {

    ProjectRequest submitRequest(String fullName, String countryCode, String phoneNumber,
            String email, java.util.List<String> serviceTypes,
            java.math.BigDecimal recaptchaScore, String clientIp);

    ProjectRequest getRequestById(Long id);

    Page<ProjectRequest> getAllRequests(Pageable pageable);

    Page<ProjectRequest> getRequestsByStatus(ProjectStatusEnum status, Pageable pageable);

    ProjectRequest updateStatus(Long id, ProjectStatusEnum newStatus);

    void softDeleteRequest(Long id);
}