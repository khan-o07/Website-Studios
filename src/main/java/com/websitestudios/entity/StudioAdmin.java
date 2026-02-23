package com.websitestudios.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "studio_admins", uniqueConstraints = {
        @UniqueConstraint(name = "uk_admin_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_admin_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudioAdmin extends BaseEntity {

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_admin_role"))
    private StudioRole role;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "failed_attempts", nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;

    @Column(name = "lock_expires_at")
    private Instant lockExpiresAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public boolean isAccountLocked() {
        if (!isLocked)
            return false;
        if (lockExpiresAt != null && Instant.now().isAfter(lockExpiresAt)) {
            this.isLocked = false;
            this.failedAttempts = 0;
            this.lockExpiresAt = null;
            return false;
        }
        return true;
    }

    public int incrementFailedAttempts() {
        this.failedAttempts = this.failedAttempts + 1;
        return this.failedAttempts;
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.isLocked = false;
        this.lockExpiresAt = null;
    }

    public void lockAccount(long lockDurationMinutes) {
        this.isLocked = true;
        this.lockExpiresAt = Instant.now().plusSeconds(lockDurationMinutes * 60);
    }

    public void recordSuccessfulLogin(String ipAddress) {
        resetFailedAttempts();
        this.lastLoginAt = Instant.now();
        this.lastLoginIp = ipAddress;
    }
}