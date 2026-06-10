CREATE TABLE transfers
(
    id              UUID PRIMARY KEY,
    from_account_id UUID           NOT NULL,
    to_account_id   UUID           NOT NULL,
    owner_username  VARCHAR(255)   NOT NULL,
    amount          NUMERIC(19, 4) NOT NULL,
    status          VARCHAR(20)    NOT NULL,
    failure_reason  VARCHAR(500),
    created_at      TIMESTAMPTZ    NOT NULL,
    updated_at      TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_transfers_owner_username ON transfers (owner_username);
CREATE INDEX idx_transfers_status ON transfers (status);