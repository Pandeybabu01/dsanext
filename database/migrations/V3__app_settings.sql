-- Flyway Migration V3: Seed App Settings (requires admin user to exist)
-- Run seed.sql first to create the admin user, then this migration handles settings.

INSERT INTO app_settings (setting_key, setting_value, data_type, description, is_public) VALUES
    ('app.name',                    'DSANext',                          'STRING',  'Application display name',                        TRUE),
    ('app.tagline',                 'Master DSA. Land the job.',        'STRING',  'App tagline shown on landing page',               TRUE),
    ('app.logo_url',                '/logo.svg',                        'STRING',  'Path or URL to the application logo',             TRUE),
    ('app.maintenance_mode',        'false',                            'BOOLEAN', 'Enable maintenance mode site-wide',               TRUE),
    ('app.maintenance_message',     'We are upgrading. Back shortly!',  'STRING',  'Message shown during maintenance',                TRUE),
    ('feature.notes_enabled',       'true',                             'BOOLEAN', 'Enable the Notes feature for users',              TRUE),
    ('feature.bookmarks_enabled',   'true',                             'BOOLEAN', 'Enable the Bookmarks feature for users',          TRUE),
    ('feature.leaderboard_enabled', 'false',                            'BOOLEAN', 'Enable the Leaderboard',                         TRUE),
    ('feature.contest_mode',        'false',                            'BOOLEAN', 'Enable Contest Mode',                            TRUE),
    ('security.jwt_expiry_minutes', '1440',                             'INTEGER', 'JWT access token expiry in minutes',             FALSE),
    ('security.refresh_expiry_days','30',                               'INTEGER', 'JWT refresh token expiry in days',               FALSE),
    ('security.max_login_attempts', '5',                                'INTEGER', 'Max failed login attempts before lockout',       FALSE),
    ('security.password_min_length','8',                                'INTEGER', 'Minimum password length policy',                 TRUE),
    ('security.require_special_char','true',                            'BOOLEAN', 'Require special character in passwords',         TRUE),
    ('pagination.default_page_size','20',                               'INTEGER', 'Default page size for list APIs',                FALSE),
    ('pagination.max_page_size',    '100',                              'INTEGER', 'Maximum allowed page size',                      FALSE),
    ('logs.retention_days',         '90',                               'INTEGER', 'Activity log retention period in days',          FALSE),
    ('logs.admin_actions_enabled',  'true',                             'BOOLEAN', 'Log all admin actions',                         FALSE);
