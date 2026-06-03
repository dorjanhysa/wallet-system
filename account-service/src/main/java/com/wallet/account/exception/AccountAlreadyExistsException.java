package com.wallet.account.exception;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException(String ownerUsername) {
        super("Account already exists for user: " + ownerUsername);
    }
}
