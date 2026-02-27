-- ═══════════════════════════════════════════════════════════════
--  V5: Create audit_trail table
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS audit_trail (
    id              BIGSERIAL PRIMARY KEY,
    admin_user_id   BIGINT REFERENCES studio_admins(id) ON DELETE SET NULL,
    action          VARCHAR(50)  NOT NULL,
    target_entity   VARCHAR(50)  NOT NULL,
    target_id       BIGINT,
    old_value       TEXT,
    new_value       TEXT,
    ip_address      VARCHAR(45)  NOT NULL,
    user_agent      VARCHAR(500),
    details         TEXT,
    performed_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Indexes for common admin queries
CREATE INDEX idx_audit_admin      ON audit_trail(admin_user_id, performed_at DESC);
CREATE INDEX idx_audit_action     ON audit_trail(action, performed_at DESC);
CREATE INDEX idx_audit_target     ON audit_trail(target_entity, target_id);
CREATE INDEX idx_audit_time       ON audit_trail(performed_at DESC);
CREATE INDEX idx_audit_ip         ON audit_trail(ip_address, performed_at DESC);

-- ═══════════════════════════════════════════════════════════════
--  V6: Create login_attempts table
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS login_attempts (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL,
    ip_address      VARCHAR(45)  NOT NULL,
    success         BOOLEAN      NOT NULL,
    failure_reason  VARCHAR(100),
    user_agent      VARCHAR(500),
    attempted_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_login_username_time
    ON login_attempts(username, attempted_at DESC);
CREATE INDEX idx_login_ip_time
    ON login_attempts(ip_address, attempted_at DESC);