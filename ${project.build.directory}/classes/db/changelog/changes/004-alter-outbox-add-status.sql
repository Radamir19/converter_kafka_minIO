--liquibase formatted sql
--changeset converter-service:4
ALTER TABLE outbox ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PROCESSED';
