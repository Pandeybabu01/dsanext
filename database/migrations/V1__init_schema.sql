-- Flyway Migration V1: Initial DSANext Schema
-- This file is identical to schema.sql for Flyway versioned migrations.
-- Flyway tracks this in the flyway_schema_history table.

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ENUMS
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE difficulty_level AS ENUM ('EASY', 'MEDIUM', 'HARD');
CREATE TYPE progress_status AS ENUM ('NOT_STARTED', 'IN_PROGRESS', 'SOLVED', 'REVISIT');
CREATE TYPE setting_data_type AS ENUM ('STRING', 'BOOLEAN', 'INTEGER', 'JSON');
CREATE TYPE notification_type AS ENUM ('INFO', 'SUCCESS', 'WARNING', 'SYSTEM');

CREATE TABLE users (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    username         VARCHAR(50)  NOT NULL UNIQUE,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    full_name        VARCHAR(150) NOT NULL,
    profile_image_url VARCHAR(500),
    role             user_role   NOT NULL DEFAULT 'USER',
    is_active        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_users_email    ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role     ON users(role);
CREATE INDEX idx_users_active   ON users(is_active);

CREATE TABLE platforms (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    base_url    VARCHAR(500) NOT NULL,
    icon_url    VARCHAR(500),
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_platforms_active ON platforms(is_active);

CREATE TABLE problems (
    id           UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    title        VARCHAR(300)     NOT NULL,
    slug         VARCHAR(350)     NOT NULL UNIQUE,
    description  TEXT,
    topic        VARCHAR(100)     NOT NULL,
    difficulty   difficulty_level NOT NULL,
    external_url VARCHAR(500),
    platform_id  UUID             REFERENCES platforms(id) ON DELETE SET NULL,
    is_active    BOOLEAN          NOT NULL DEFAULT TRUE,
    created_by   UUID             REFERENCES users(id) ON DELETE SET NULL,
    created_at   TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_problems_difficulty  ON problems(difficulty);
CREATE INDEX idx_problems_topic       ON problems(topic);
CREATE INDEX idx_problems_platform_id ON problems(platform_id);
CREATE INDEX idx_problems_active      ON problems(is_active);
CREATE INDEX idx_problems_slug        ON problems(slug);
CREATE INDEX idx_problems_created_at  ON problems(created_at DESC);

CREATE TABLE progress (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id          UUID            NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    status              progress_status NOT NULL DEFAULT 'NOT_STARTED',
    attempt_count       INT             NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    first_attempted_at  TIMESTAMPTZ,
    solved_at           TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_progress_user_problem UNIQUE (user_id, problem_id)
);
CREATE INDEX idx_progress_user_id    ON progress(user_id);
CREATE INDEX idx_progress_problem_id ON progress(problem_id);
CREATE INDEX idx_progress_status     ON progress(status);
CREATE INDEX idx_progress_solved_at  ON progress(solved_at DESC);

CREATE TABLE notes (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id  UUID        NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    content     TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_notes_user_problem UNIQUE (user_id, problem_id)
);
CREATE INDEX idx_notes_user_id    ON notes(user_id);
CREATE INDEX idx_notes_problem_id ON notes(problem_id);
CREATE INDEX idx_notes_updated_at ON notes(updated_at DESC);

CREATE TABLE bookmarks (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id  UUID        NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_bookmarks_user_problem UNIQUE (user_id, problem_id)
);
CREATE INDEX idx_bookmarks_user_id    ON bookmarks(user_id);
CREATE INDEX idx_bookmarks_problem_id ON bookmarks(problem_id);
CREATE INDEX idx_bookmarks_created_at ON bookmarks(created_at DESC);

CREATE TABLE notifications (
    id          UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID              NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(200)      NOT NULL,
    message     TEXT              NOT NULL,
    type        notification_type NOT NULL DEFAULT 'INFO',
    is_read     BOOLEAN           NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notifications_user_id    ON notifications(user_id);
CREATE INDEX idx_notifications_is_read    ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

CREATE TABLE user_settings (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID        NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    theme                 VARCHAR(20) NOT NULL DEFAULT 'light' CHECK (theme IN ('light', 'dark', 'system')),
    notifications_enabled BOOLEAN     NOT NULL DEFAULT TRUE,
    email_notifications   BOOLEAN     NOT NULL DEFAULT TRUE,
    lc_username           VARCHAR(100),
    cf_username           VARCHAR(100),
    hr_username           VARCHAR(100),
    ib_username           VARCHAR(100),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_user_settings_user_id ON user_settings(user_id);

CREATE TABLE app_settings (
    id            UUID               PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key   VARCHAR(100)       NOT NULL UNIQUE,
    setting_value TEXT               NOT NULL,
    data_type     setting_data_type  NOT NULL DEFAULT 'STRING',
    description   VARCHAR(300),
    is_public     BOOLEAN            NOT NULL DEFAULT FALSE,
    updated_by    UUID               REFERENCES users(id) ON DELETE SET NULL,
    updated_at    TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_app_settings_key    ON app_settings(setting_key);
CREATE INDEX idx_app_settings_public ON app_settings(is_public);

CREATE TABLE activity_logs (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        REFERENCES users(id) ON DELETE SET NULL,
    action       VARCHAR(100) NOT NULL,
    entity_type  VARCHAR(100),
    entity_id    VARCHAR(36),
    metadata     JSONB,
    ip_address   VARCHAR(45),
    user_agent   VARCHAR(500),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_activity_logs_user_id    ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_action     ON activity_logs(action);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at DESC);
CREATE INDEX idx_activity_logs_entity     ON activity_logs(entity_type, entity_id);

-- Auto-update triggers
CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at          BEFORE UPDATE ON users          FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
CREATE TRIGGER trg_problems_updated_at       BEFORE UPDATE ON problems       FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
CREATE TRIGGER trg_platforms_updated_at      BEFORE UPDATE ON platforms      FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
CREATE TRIGGER trg_progress_updated_at       BEFORE UPDATE ON progress       FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
CREATE TRIGGER trg_notes_updated_at          BEFORE UPDATE ON notes          FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
CREATE TRIGGER trg_user_settings_updated_at  BEFORE UPDATE ON user_settings  FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
CREATE TRIGGER trg_app_settings_updated_at   BEFORE UPDATE ON app_settings   FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
