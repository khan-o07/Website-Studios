-- ═══════════════════════════════════════════════════════════
--  Website Studios — V1: Project Requests Tables
-- ═══════════════════════════════════════════════════════════

CREATE TABLE project_requests (
    id                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    full_name         VARCHAR(150)    NOT NULL,
    country_code      VARCHAR(10)     NOT NULL,
    phone_number      VARCHAR(500)    NOT NULL,
    email             VARCHAR(500)    NOT NULL,
    email_hash        VARCHAR(64)     NOT NULL,
    phone_hash        VARCHAR(64)     NOT NULL,
    status            VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    is_deleted        BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMP       NULL,
    recaptcha_score   DECIMAL(3,2)    NULL,
    client_ip         VARCHAR(64)     NULL,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_project_request_email_phone
        UNIQUE (email_hash, phone_hash),

    CONSTRAINT chk_project_request_status
        CHECK (status IN ('PENDING', 'REVIEWING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_project_request_email_hash ON project_requests (email_hash);
CREATE INDEX idx_project_request_status ON project_requests (status);
CREATE INDEX idx_project_request_created ON project_requests (created_at);
CREATE INDEX idx_project_request_not_deleted ON project_requests (is_deleted);

-- Service types join table
CREATE TABLE project_service_types (
    id                    BIGINT          AUTO_INCREMENT PRIMARY KEY,
    project_request_id    BIGINT          NOT NULL,
    project_type          VARCHAR(30)     NOT NULL,

    CONSTRAINT fk_pst_project_request
        FOREIGN KEY (project_request_id)
        REFERENCES project_requests (id)
        ON DELETE CASCADE,

    CONSTRAINT chk_project_type
        CHECK (project_type IN ('ANDROID_APP', 'IOS_APP', 'WEBSITE'))
);

CREATE INDEX idx_pst_request_id ON project_service_types (project_request_id);