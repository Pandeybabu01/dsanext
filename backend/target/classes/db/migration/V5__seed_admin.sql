
-- INSERT INTO users (
--     full_name,
--     username,
--     email,
--     password_hash,
--     role,
--     is_active,
--     created_at,
--     updated_at
-- )
-- VALUES (
--            'System Admin',
--            'admin',
--            'admin@dsanext.com',
--            '$2a$12$j6Q6oTnZ0cbAqdhYaErEEO3FzGgP3.hExvLR8ZMxTe5Gy9A86dxGO',
--            'ADMIN',
--            true,
--            NOW(),
--            NOW()
--        );


INSERT INTO users (
    full_name,
    username,
    email,
    password_hash,
    role,
    is_active,
    created_at,
    updated_at
)
SELECT
    'System Admin',
    'admin',
    'admin@dsanext.com',
    '$2a$12$j6Q6oTnZ0cbAqdhYaErEEO3FzGgP3.hExvLR8ZMxTe5Gy9A86dxGO',
    'ADMIN',
    true,
    NOW(),
    NOW()
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@dsanext.com'
);