package com.wallet.account.mapper;

import com.wallet.account.domain.Account;
import com.wallet.account.domain.Transaction;
import com.wallet.account.dto.AccountResponse;
import com.wallet.account.dto.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOwnerUsername(),
                account.getBalance(),
                account.getCurrency()
        );
    }

    public TransactionResponse toTransactionResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getType(),
                tx.getAmount(),
                tx.getBalanceAfter(),
                tx.getCreatedAt()
        );
    }
}
