package com.wallet.account.dto;

import com.wallet.account.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(

        UUID id,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant createdAt
) {}
