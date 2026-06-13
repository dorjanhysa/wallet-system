package com.wallet.transaction.event;

import java.util.UUID;

public record CommandResult(

        UUID transferId,
        UUID accountId,
        String step,
        boolean success,
        String failureReason
) {
}
