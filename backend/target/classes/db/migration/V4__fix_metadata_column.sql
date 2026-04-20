-- Flyway Migration V4: Change metadata column from jsonb to text
-- This fixes Hibernate 6 + Java 17 JSONB compatibility issues

ALTER TABLE activity_logs
ALTER COLUMN metadata TYPE TEXT
    USING metadata::TEXT;