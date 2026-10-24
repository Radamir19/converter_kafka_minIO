--liquibase formatted sql
--changeset converter-service:1
CREATE TABLE outbox (
    id           UUID PRIMARY KEY,            -- id события (станет ключом дедупликации у получателя)
    aggregate    VARCHAR(100) NOT NULL,       -- тип сущности, напр. "order"
    payload      VARCHAR NOT NULL,              -- тело события
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ                  -- NULL = ещё не опубликовано
);
--rollback DROP TABLE outbox;