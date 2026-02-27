-- ═══════════════════════════════════════════════════════════════
--  Admin Seed Data — Default admin user for development/testing
-- ═══════════════════════════════════════════════════════════════
--
--  Password: Admin@123456
--  BCrypt Hash (strength 12): Generated via BCryptPasswordEncoder
--
--  ⚠️  CHANGE THIS PASSWORD IN PRODUCTION!
--  ⚠️  NEVER use default credentials in production.
--
--  To generate a new BCrypt hash:
--    In Java: new BCryptPasswordEncoder(12).encode("YourNewPassword")
--    Online:  https://bcrypt-generator.com/  (use 12 rounds)

-- Insert roles first
INSERT INTO studio_roles (role_name, permissions, created_at)
VALUES
    ('ADMIN', ARRAY['READ_REQUESTS', 'WRITE_REQUESTS', 'VIEW_DASHBOARD'], NOW()),
    ('SUPER_ADMIN', ARRAY['READ_REQUESTS', 'WRITE_REQUESTS', 'DELETE_REQUESTS',
                           'MANAGE_USERS', 'VIEW_AUDIT_TRAIL', 'EXPORT_DATA',
                           'VIEW_DASHBOARD'], NOW())
ON CONFLICT (role_name) DO NOTHING;

-- Insert default super admin user
-- Username: superadmin
-- Password: Admin@123456  (BCrypt hash below)
INSERT INTO studio_admins (
    username,
    email,
    password_hash,
    role_id,
    is_locked,
    failed_attempts,
    is_active,
    created_at,
    updated_at
)
SELECT
    'superadmin',
    'admin@websitestudios.com',
    '\$2a$12$LJ3m4ys3VzBFBMqPOKNOyOY6JFKV1rR8JVjH8N0s7L2GdFHnXPHKi',
    r.id,
    false,
    0,
    true,
    NOW(),
    NOW()
FROM studio_roles r
WHERE r.role_name = 'SUPER_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM studio_admins WHERE username = 'superadmin'
);

-- Insert default admin user
-- Username: admin
-- Password: Admin@123456  (BCrypt hash below)
INSERT INTO studio_admins (
    username,
    email,
    password_hash,
    role_id,
    is_locked,
    failed_attempts,
    is_active,
    created_at,
    updated_at
)
SELECT
    'admin',
    'staff@websitestudios.com',
    '\$2a$12$LJ3m4ys3VzBFBMqPOKNOyOY6JFKV1rR8JVjH8N0s7L2GdFHnXPHKi',
    r.id,
    false,
    0,
    true,
    NOW(),
    NOW()
FROM studio_roles r
WHERE r.role_name = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM studio_admins WHERE username = 'admin'
);