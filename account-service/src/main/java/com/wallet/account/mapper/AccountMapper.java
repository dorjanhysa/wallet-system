package com.wallet.account.mapper;

import com.wallet.account.domain.Account;
import com.wallet.account.dto.AccountResponse;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOwnerUsername(),
                account.getBalance(),
                account.getCurrency()
        );
    }
}
