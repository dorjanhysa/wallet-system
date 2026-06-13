package com.wallet.account.event;

import java.util.UUID;

public record CommandResult(

        UUID transferId,
        UUID accountId,
        String step,
        boolean success,
        String failureReason
) {}
