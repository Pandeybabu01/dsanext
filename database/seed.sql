-- =============================================================
--  DSANext — Seed Data
--  Run AFTER schema.sql
--  Admin password: Admin@123 (BCrypt hash below)
-- =============================================================

-- =============================================================
-- PLATFORMS
-- =============================================================

INSERT INTO platforms (id, name, base_url, icon_url, is_active) VALUES
    (gen_random_uuid(), 'LeetCode',    'https://leetcode.com/problems/',          'https://assets.leetcode.com/static_assets/public/icons/favicon.ico', TRUE),
    (gen_random_uuid(), 'Codeforces',  'https://codeforces.com/problemset/problem/', 'https://codeforces.org/s/0/images/codeforces-logo-with-telecom.png', TRUE),
    (gen_random_uuid(), 'HackerRank',  'https://www.hackerrank.com/challenges/',  'https://hrcdn.net/fcore/assets/favicon-ddc852f75a.png', TRUE),
    (gen_random_uuid(), 'InterviewBit','https://www.interviewbit.com/problems/',  'https://www.interviewbit.com/favicon.ico', TRUE),
    (gen_random_uuid(), 'GeeksforGeeks','https://www.geeksforgeeks.org/',         'https://media.geeksforgeeks.org/gfg-gg-logo.svg', TRUE);

-- =============================================================
-- ADMIN USER
-- Password: Admin@123 (BCrypt, cost 10)
-- =============================================================

INSERT INTO users (id, username, email, password_hash, full_name, role, is_active)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'dsanext_admin',
    'admin@dsanext.com',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOZjjWTRkqKZH8b8oCKM1HQKxsLf6Xy5i',
    'DSANext Admin',
    'ADMIN',
    TRUE
);

-- Auto-create user_settings for admin
INSERT INTO user_settings (user_id, theme, notifications_enabled, email_notifications)
VALUES ('a0000000-0000-0000-0000-000000000001', 'light', TRUE, TRUE);

-- =============================================================
-- SAMPLE PROBLEMS
-- =============================================================

-- Store platform IDs in a temp variable approach via a CTE
WITH plat AS (
    SELECT id, name FROM platforms
)
INSERT INTO problems (id, title, slug, description, topic, difficulty, external_url, platform_id, is_active, created_by)
SELECT
    gen_random_uuid(),
    p.title,
    p.slug,
    p.description,
    p.topic,
    p.difficulty::difficulty_level,
    p.external_url,
    plat.id,
    TRUE,
    'a0000000-0000-0000-0000-000000000001'
FROM (VALUES
    ('Two Sum',
     'two-sum',
     'Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.',
     'Array', 'EASY',
     'https://leetcode.com/problems/two-sum/',
     'LeetCode'),

    ('Best Time to Buy and Sell Stock',
     'best-time-to-buy-and-sell-stock',
     'You are given an array prices where prices[i] is the price of a given stock on the ith day. Maximize profit.',
     'Array', 'EASY',
     'https://leetcode.com/problems/best-time-to-buy-and-sell-stock/',
     'LeetCode'),

    ('Longest Substring Without Repeating Characters',
     'longest-substring-without-repeating-characters',
     'Given a string s, find the length of the longest substring without repeating characters.',
     'Sliding Window', 'MEDIUM',
     'https://leetcode.com/problems/longest-substring-without-repeating-characters/',
     'LeetCode'),

    ('Add Two Numbers',
     'add-two-numbers',
     'You are given two non-empty linked lists representing two non-negative integers. Add the two numbers and return the sum as a linked list.',
     'Linked List', 'MEDIUM',
     'https://leetcode.com/problems/add-two-numbers/',
     'LeetCode'),

    ('Median of Two Sorted Arrays',
     'median-of-two-sorted-arrays',
     'Given two sorted arrays nums1 and nums2 of size m and n respectively, return the median of the two sorted arrays.',
     'Binary Search', 'HARD',
     'https://leetcode.com/problems/median-of-two-sorted-arrays/',
     'LeetCode'),

    ('Climbing Stairs',
     'climbing-stairs',
     'You are climbing a staircase. It takes n steps to reach the top. Each time you can either climb 1 or 2 steps.',
     'Dynamic Programming', 'EASY',
     'https://leetcode.com/problems/climbing-stairs/',
     'LeetCode'),

    ('Coin Change',
     'coin-change',
     'You are given an integer array coins representing coins of various denominations and an integer amount. Return the fewest coins needed.',
     'Dynamic Programming', 'MEDIUM',
     'https://leetcode.com/problems/coin-change/',
     'LeetCode'),

    ('Number of Islands',
     'number-of-islands',
     'Given an m x n 2D binary grid which represents a map of 1s (land) and 0s (water), return the number of islands.',
     'Graph', 'MEDIUM',
     'https://leetcode.com/problems/number-of-islands/',
     'LeetCode'),

    ('Word Ladder',
     'word-ladder',
     'A transformation sequence from word beginWord to word endWord using a dictionary wordList is a sequence of words.',
     'Graph', 'HARD',
     'https://leetcode.com/problems/word-ladder/',
     'LeetCode'),

    ('Binary Tree Level Order Traversal',
     'binary-tree-level-order-traversal',
     'Given the root of a binary tree, return the level order traversal of its nodes values.',
     'Tree', 'MEDIUM',
     'https://leetcode.com/problems/binary-tree-level-order-traversal/',
     'LeetCode'),

    ('Merge K Sorted Lists',
     'merge-k-sorted-lists',
     'You are given an array of k linked-lists lists, each linked-list is sorted in ascending order. Merge all the linked-lists into one sorted linked-list.',
     'Linked List', 'HARD',
     'https://leetcode.com/problems/merge-k-sorted-lists/',
     'LeetCode'),

    ('Valid Parentheses',
     'valid-parentheses',
     'Given a string s containing just the characters (, ), {, }, [ and ], determine if the input string is valid.',
     'Stack', 'EASY',
     'https://leetcode.com/problems/valid-parentheses/',
     'LeetCode'),

    ('Trapping Rain Water',
     'trapping-rain-water',
     'Given n non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.',
     'Two Pointers', 'HARD',
     'https://leetcode.com/problems/trapping-rain-water/',
     'LeetCode'),

    ('Longest Common Subsequence',
     'longest-common-subsequence',
     'Given two strings text1 and text2, return the length of their longest common subsequence.',
     'Dynamic Programming', 'MEDIUM',
     'https://leetcode.com/problems/longest-common-subsequence/',
     'LeetCode'),

    ('Maximum Subarray',
     'maximum-subarray',
     'Given an integer array nums, find the subarray which has the largest sum and return its sum.',
     'Array', 'MEDIUM',
     'https://leetcode.com/problems/maximum-subarray/',
     'LeetCode')
) AS p(title, slug, description, topic, difficulty, external_url, platform_name)
JOIN plat ON plat.name = p.platform_name;

-- =============================================================
-- APP SETTINGS (Admin-controlled global config)
-- =============================================================

INSERT INTO app_settings (setting_key, setting_value, data_type, description, is_public, updated_by) VALUES
    -- Branding
    ('app.name',               'DSANext',           'STRING',  'Application display name',                TRUE,  'a0000000-0000-0000-0000-000000000001'),
    ('app.tagline',            'Master DSA. Land the job.', 'STRING', 'App tagline shown on landing page', TRUE, 'a0000000-0000-0000-0000-000000000001'),
    ('app.logo_url',           '/logo.svg',         'STRING',  'Path or URL to the application logo',     TRUE,  'a0000000-0000-0000-0000-000000000001'),

    -- Maintenance
    ('app.maintenance_mode',   'false',             'BOOLEAN', 'Enable maintenance mode site-wide',        TRUE,  'a0000000-0000-0000-0000-000000000001'),
    ('app.maintenance_message','We are upgrading DSANext. Back shortly!', 'STRING', 'Message shown during maintenance', TRUE, 'a0000000-0000-0000-0000-000000000001'),

    -- Feature toggles
    ('feature.notes_enabled',       'true',  'BOOLEAN', 'Enable the Notes feature for users',        TRUE,  'a0000000-0000-0000-0000-000000000001'),
    ('feature.bookmarks_enabled',   'true',  'BOOLEAN', 'Enable the Bookmarks feature for users',    TRUE,  'a0000000-0000-0000-0000-000000000001'),
    ('feature.leaderboard_enabled', 'false', 'BOOLEAN', 'Enable the Leaderboard (coming soon)',      TRUE,  'a0000000-0000-0000-0000-000000000001'),
    ('feature.contest_mode',        'false', 'BOOLEAN', 'Enable Contest Mode',                       TRUE,  'a0000000-0000-0000-0000-000000000001'),

    -- Security
    ('security.jwt_expiry_minutes',    '1440',  'INTEGER', 'JWT access token expiry in minutes (default 24h)', FALSE, 'a0000000-0000-0000-0000-000000000001'),
    ('security.refresh_expiry_days',   '30',    'INTEGER', 'JWT refresh token expiry in days',              FALSE, 'a0000000-0000-0000-0000-000000000001'),
    ('security.max_login_attempts',    '5',     'INTEGER', 'Max failed login attempts before lockout',      FALSE, 'a0000000-0000-0000-0000-000000000001'),
    ('security.password_min_length',   '8',     'INTEGER', 'Minimum password length policy',               TRUE,  'a0000000-0000-0000-0000-000000000001'),
    ('security.require_special_char',  'true',  'BOOLEAN', 'Require special character in passwords',       TRUE,  'a0000000-0000-0000-0000-000000000001'),

    -- Pagination defaults
    ('pagination.default_page_size',   '20',    'INTEGER', 'Default page size for list APIs',              FALSE, 'a0000000-0000-0000-0000-000000000001'),
    ('pagination.max_page_size',       '100',   'INTEGER', 'Maximum allowed page size',                    FALSE, 'a0000000-0000-0000-0000-000000000001'),

    -- Logs
    ('logs.retention_days',            '90',    'INTEGER', 'Activity log retention period in days',        FALSE, 'a0000000-0000-0000-0000-000000000001'),
    ('logs.admin_actions_enabled',     'true',  'BOOLEAN', 'Log all admin actions',                        FALSE, 'a0000000-0000-0000-0000-000000000001');

-- =============================================================
-- WELCOME NOTIFICATION for Admin
-- =============================================================

INSERT INTO notifications (user_id, title, message, type, is_read)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'Welcome to DSANext Admin Panel',
    'Your admin account is set up. Start by adding problems and configuring platforms.',
    'SUCCESS',
    FALSE
);
