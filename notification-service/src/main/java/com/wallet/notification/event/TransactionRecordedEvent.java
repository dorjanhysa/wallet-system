package com.wallet.notification.event;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRecordedEvent(

        UUID accountId,
        String ownerUsername,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter

) {}
