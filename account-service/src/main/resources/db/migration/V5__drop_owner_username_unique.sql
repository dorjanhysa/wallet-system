ALTER TABLE accounts DROP CONSTRAINT accounts_owner_username_key;
CREATE INDEX idx_accounts_owner_username ON accounts (owner_username);