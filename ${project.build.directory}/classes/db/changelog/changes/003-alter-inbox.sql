--luqibase formatted sql
--changeset converter-service:3
ALTER TABLE inbox ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED';
ALTER TABLE inbox ALTER COLUMN processed_at DROP NOT NULL;
ALTER TABLE inbox ALTER COLUMN processed_at DROP DEFAULT;