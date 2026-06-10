package com.wallet.transaction.domain;

public enum TransferStatus {

    PENDING,
    DEBITED,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}
