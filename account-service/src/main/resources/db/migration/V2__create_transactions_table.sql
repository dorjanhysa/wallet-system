CREATE TABLE transactions
(
    id            UUID PRIMARY KEY,
    account_id    UUID           NOT NULL,
    type          VARCHAR(20)    NOT NULL,
    amount        NUMERIC(19, 4) NOT NULL,
    balance_after NUMERIC(19, 4) NOT NULL,
    created_at    TIMESTAMP      NOT NULL,
    updated_at    TIMESTAMP      NOT NULL,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE INDEX idx_transactions_account_id ON transactions (account_id);