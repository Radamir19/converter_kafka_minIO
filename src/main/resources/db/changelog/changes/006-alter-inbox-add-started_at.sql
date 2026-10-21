--liquibase formatted sql
--changeset converter-service:6
ALTER TABLE inbox ADD COLUMN started_at TIMESTAMPTZ;