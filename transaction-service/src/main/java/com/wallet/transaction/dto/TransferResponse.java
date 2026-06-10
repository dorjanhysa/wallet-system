package com.wallet.transaction.dto;

import com.wallet.transaction.domain.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(

        UUID id,
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount,
        TransferStatus status,
        String failureReason,
        Instant createdAt
) {}
