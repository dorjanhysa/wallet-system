package com.wallet.account.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String owner, BigDecimal balance, BigDecimal requested) {
        super("Insufficient funds for account '%s': balance=%s, requested=%s"
                .formatted(owner, balance, requested));
    }
}
