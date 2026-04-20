-- Flyway Migration V2: Seed Platform Data
INSERT INTO platforms (id, name, base_url, icon_url, is_active) VALUES
    (gen_random_uuid(), 'LeetCode',     'https://leetcode.com/problems/',              'https://assets.leetcode.com/static_assets/public/icons/favicon.ico', TRUE),
    (gen_random_uuid(), 'Codeforces',   'https://codeforces.com/problemset/problem/',  'https://codeforces.org/s/0/images/codeforces-logo-with-telecom.png', TRUE),
    (gen_random_uuid(), 'HackerRank',   'https://www.hackerrank.com/challenges/',      'https://hrcdn.net/fcore/assets/favicon-ddc852f75a.png', TRUE),
    (gen_random_uuid(), 'InterviewBit', 'https://www.interviewbit.com/problems/',      'https://www.interviewbit.com/favicon.ico', TRUE),
    (gen_random_uuid(), 'GeeksforGeeks','https://www.geeksforgeeks.org/',              'https://media.geeksforgeeks.org/gfg-gg-logo.svg', TRUE);
