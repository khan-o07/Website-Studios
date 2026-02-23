-- ═══════════════════════════════════════════════════════════
--  Website Studios — V6: Login Attempts Table
-- ═══════════════════════════════════════════════════════════

CREATE TABLE studio_login_attempts (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY,
    username         VARCHAR(50)     NOT NULL,
    ip_address       VARCHAR(45)     NOT NULL,
    success          BOOLEAN         NOT NULL,
    failure_reason   VARCHAR(100)    NULL,
    attempted_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_login_username_time ON studio_login_attempts (username, attempted_at);
CREATE INDEX idx_login_ip_time ON studio_login_attempts (ip_address, attempted_at);