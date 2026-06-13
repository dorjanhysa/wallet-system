DROP TABLE processed_commands;

CREATE TABLE processed_commands
(
    transfer_id  UUID         NOT NULL,
    command_type VARCHAR(20)  NOT NULL,
    result_type  VARCHAR(100) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (transfer_id, command_type)
);