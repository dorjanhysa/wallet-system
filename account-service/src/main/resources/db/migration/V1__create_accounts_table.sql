CREATE TABLE accounts
(
    id             UUID PRIMARY KEY,
    owner_username VARCHAR(255)   NOT NULL UNIQUE,
    balance        NUMERIC(19, 4) NOT NULL,
    currency       VARCHAR(3)     NOT NULL,
    version        BIGINT         NOT NULL,
    created_at     TIMESTAMP      NOT NULL,
    updated_at     TIMESTAMP      NOT NULL
);