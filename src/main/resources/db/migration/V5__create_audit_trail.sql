-- ═══════════════════════════════════════════════════════════
--  Website Studios — V5: Audit Trail Table
-- ═══════════════════════════════════════════════════════════

CREATE TABLE studio_audit_trail (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY,
    admin_id         BIGINT          NULL,
    action           VARCHAR(50)     NOT NULL,
    target_entity    VARCHAR(50)     NOT NULL,
    target_id        BIGINT          NULL,
    old_value        VARCHAR(2000)   NULL,
    new_value        VARCHAR(2000)   NULL,
    ip_address       VARCHAR(45)     NOT NULL,
    user_agent       VARCHAR(500)    NULL,
    performed_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_admin
        FOREIGN KEY (admin_id)
        REFERENCES studio_admins (id)
);

CREATE INDEX idx_audit_admin_id ON studio_audit_trail (admin_id);
CREATE INDEX idx_audit_action ON studio_audit_trail (action);
CREATE INDEX idx_audit_performed_at ON studio_audit_trail (performed_at);
CREATE INDEX idx_audit_target ON studio_audit_trail (target_entity, target_id);