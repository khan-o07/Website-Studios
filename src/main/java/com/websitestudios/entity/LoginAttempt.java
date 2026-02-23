package com.websitestudios.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "studio_login_attempts", indexes = {
        @Index(name = "idx_login_username_time", columnList = "username, attempted_at"),
        @Index(name = "idx_login_ip_time", columnList = "ip_address, attempted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "failure_reason", length = 100)
    private String failureReason;

    @Column(name = "attempted_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant attemptedAt = Instant.now();
}