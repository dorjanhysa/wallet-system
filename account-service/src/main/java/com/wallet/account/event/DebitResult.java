package com.wallet.account.event;

import java.util.UUID;

public record DebitResult(

        UUID transferId,
        UUID accountId,
        boolean success,
        String failureReason
) {}
