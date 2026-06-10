package com.wallet.transaction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transfers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer extends BaseEntity {

    @Column(name = "from_account_id", nullable = false)
    private UUID fromAccountId;

    @Column(name = "to_account_id", nullable = false)
    private UUID toAccountId;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    public Transfer(UUID fromAccountId, UUID toAccountId, String ownerUsername, BigDecimal amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.ownerUsername = ownerUsername;
        this.amount = amount;
        this.status = TransferStatus.PENDING;
    }

    public void markDebited() {
        this.status = TransferStatus.DEBITED;
    }

    public void markCompleted() {
        this.status = TransferStatus.COMPLETED;
    }

    public void markFailed(String reason) {
        this.status = TransferStatus.FAILED;
        this.failureReason = reason;
    }

    public void markCompensating() {
        this.status = TransferStatus.COMPENSATING;
    }

    public void markCompensated() {
        this.status = TransferStatus.COMPENSATED;
    }
}
