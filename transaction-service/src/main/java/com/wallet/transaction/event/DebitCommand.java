package com.wallet.transaction.event;

import java.math.BigDecimal;
import java.util.UUID;

public record DebitCommand(

        UUID transferId,
        UUID accountId,
        String ownerUsername,
        BigDecimal amount
) {}
