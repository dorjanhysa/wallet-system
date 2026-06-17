package com.wallet.account.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_commands")
@IdClass(ProcessedCommandId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedCommand {

    @Id
    @Column(name = "transfer_id")
    private UUID transferId;

    @Id
    @Column(name = "command_type")
    private String commandType;

    @Column(name = "result_type", nullable = false)
    private String resultType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ProcessedCommand(UUID transferId, String commandType, String resultType) {
        this.transferId = transferId;
        this.commandType = commandType;
        this.resultType = resultType;
        this.createdAt = Instant.now();
    }
}
