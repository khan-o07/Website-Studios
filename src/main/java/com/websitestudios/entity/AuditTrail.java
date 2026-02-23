package com.websitestudios.entity;

import com.websitestudios.enums.AuditActionEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Records every significant admin action in the Website Studios system.
 * This is an append-only table — records are NEVER updated or deleted.
 *
 * Used for security auditing, compliance, and forensics.
 */
@Entity
@Table(name = "studio_audit_trail", indexes = {
        @Index(name = "idx_audit_admin_id", columnList = "admin_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_performed_at", columnList = "performed_at"),
        @Index(name = "idx_audit_target", columnList = "target_entity, target_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", foreignKey = @ForeignKey(name = "fk_audit_admin"))
    private StudioAdmin admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditActionEnum action;

    @Column(name = "target_entity", nullable = false, length = 50)
    private String targetEntity;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "old_value", length = 2000)
    private String oldValue;

    @Column(name = "new_value", length = 2000)
    private String newValue;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "performed_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant performedAt = Instant.now();
}