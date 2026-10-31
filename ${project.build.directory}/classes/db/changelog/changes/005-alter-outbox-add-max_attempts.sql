--liquibase formatted sql
--changeset converter-service:5
ALTER TABLE outbox ADD COLUMN attempts SMALLINT NOT NULL DEFAULT 0;