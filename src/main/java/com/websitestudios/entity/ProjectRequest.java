package com.websitestudios.entity;

import com.websitestudios.enums.ProjectStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_requests", uniqueConstraints = {
        @UniqueConstraint(name = "uk_project_request_email_phone", columnNames = { "email_hash", "phone_hash" })
}, indexes = {
        @Index(name = "idx_project_request_email_hash", columnList = "email_hash"),
        @Index(name = "idx_project_request_status", columnList = "status"),
        @Index(name = "idx_project_request_created", columnList = "created_at"),
        @Index(name = "idx_project_request_not_deleted", columnList = "is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequest extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "country_code", nullable = false, length = 10)
    private String countryCode;

    @Column(name = "phone_number", nullable = false, length = 500)
    private String phoneNumber;

    @Column(name = "email", nullable = false, length = 500)
    private String email;

    @Column(name = "email_hash", nullable = false, length = 64)
    private String emailHash;

    @Column(name = "phone_hash", nullable = false, length = 64)
    private String phoneHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProjectStatusEnum status = ProjectStatusEnum.PENDING;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "recaptcha_score", precision = 3, scale = 2)
    private BigDecimal recaptchaScore;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @OneToMany(mappedBy = "projectRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectServiceType> serviceTypes = new ArrayList<>();

    public void addServiceType(ProjectServiceType serviceType) {
        serviceTypes.add(serviceType);
        serviceType.setProjectRequest(this);
    }

    public void removeServiceType(ProjectServiceType serviceType) {
        serviceTypes.remove(serviceType);
        serviceType.setProjectRequest(null);
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = Instant.now();
    }
}