package com.wallet.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_commands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedCommand {

    @Id
    @Column(name = "command_id")
    private UUID commandId;

    @Column(name = "result_type", nullable = false)
    private String resultType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ProcessedCommand(UUID commandId, String resultType) {
        this.commandId = commandId;
        this.resultType = resultType;
        this.createdAt = Instant.now();
    }
}
