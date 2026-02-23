-- ═══════════════════════════════════════════════════════════
--  Website Studios — V3: Studio Roles Table
-- ═══════════════════════════════════════════════════════════

CREATE TABLE studio_roles (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    role_name       VARCHAR(30)     NOT NULL,
    permissions     VARCHAR(500)    NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_role_name UNIQUE (role_name),

    CONSTRAINT chk_role_name
        CHECK (role_name IN ('STUDIO_ADMIN', 'STUDIO_SUPER_ADMIN'))
);

-- Seed default roles (permissions as comma-separated string)
INSERT INTO studio_roles (role_name, permissions) VALUES
    ('STUDIO_ADMIN', 'READ,WRITE'),
    ('STUDIO_SUPER_ADMIN', 'READ,WRITE,DELETE,MANAGE_USERS,VIEW_AUDIT,EXPORT');