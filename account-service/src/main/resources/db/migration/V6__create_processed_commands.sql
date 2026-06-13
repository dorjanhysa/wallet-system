CREATE TABLE processed_commands (
    command_id UUID PRIMARY KEY,
    result_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);