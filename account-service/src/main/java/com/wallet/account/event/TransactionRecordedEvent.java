package com.wallet.account.event;

import com.wallet.account.domain.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRecordedEvent(

        UUID accountId,
        String ownerUsername,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter
) {}
