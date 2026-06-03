package com.wallet.account.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String ownerUsername) {
        super("Account not found for user: " + ownerUsername);
    }
}
