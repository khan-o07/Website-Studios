-- ═══════════════════════════════════════════════════════════
--  Website Studios — V4: Studio Admins Table
-- ═══════════════════════════════════════════════════════════

CREATE TABLE studio_admins (
    id                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    username          VARCHAR(50)     NOT NULL,
    email             VARCHAR(255)    NOT NULL,
    password_hash     VARCHAR(255)    NOT NULL,
    role_id           BIGINT          NOT NULL,
    is_locked         BOOLEAN         NOT NULL DEFAULT FALSE,
    failed_attempts   INT             NOT NULL DEFAULT 0,
    lock_expires_at   TIMESTAMP       NULL,
    last_login_at     TIMESTAMP       NULL,
    last_login_ip     VARCHAR(45)     NULL,
    is_active         BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_admin_username UNIQUE (username),
    CONSTRAINT uk_admin_email    UNIQUE (email),

    CONSTRAINT fk_admin_role
        FOREIGN KEY (role_id)
        REFERENCES studio_roles (id)
);

-- Seed default super admin
-- Password: WebsiteStudios@2025 (BCrypt hash)
INSERT INTO studio_admins (username, email, password_hash, role_id) VALUES
    (
        'superadmin',
        'admin@websitestudios.com',
        '\$2a$12$LJ3m4ys3Kzx2vWxPpGq1LeG.eFz5HLlPSqp1v3F7YdOARGNXdMSKW',
        (SELECT id FROM studio_roles WHERE role_name = 'STUDIO_SUPER_ADMIN')
    );