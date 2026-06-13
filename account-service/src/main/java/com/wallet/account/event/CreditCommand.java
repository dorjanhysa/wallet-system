package com.wallet.account.event;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCommand(

        UUID transferId,
        UUID accountId,
        BigDecimal amount
) {
}
