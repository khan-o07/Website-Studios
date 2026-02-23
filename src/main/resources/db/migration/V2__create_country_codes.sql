-- ═══════════════════════════════════════════════════════════
--  Website Studios — V2: Country Codes Table
--
--  NOTE: dial_code is NOT unique because multiple countries
--  share the same dial code (e.g., +1 = US, Canada, Jamaica)
--  iso_code IS unique (each country has a unique ISO code)
-- ═══════════════════════════════════════════════════════════

CREATE TABLE country_codes (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    country_name    VARCHAR(100)    NOT NULL,
    dial_code       VARCHAR(10)     NOT NULL,
    iso_code        VARCHAR(5)      NOT NULL,
    flag_emoji      VARCHAR(10)     NULL,

    CONSTRAINT uk_country_iso_code UNIQUE (iso_code)
);

CREATE INDEX idx_country_name ON country_codes (country_name);
CREATE INDEX idx_country_dial_code ON country_codes (dial_code);