--liquibase formatted sql
--changeset converter-service:2
CREATE TABLE inbox(
    event_id UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);