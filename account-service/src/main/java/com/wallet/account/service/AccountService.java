package com.wallet.account.service;

import com.wallet.account.domain.Account;
import com.wallet.account.dto.AccountResponse;
import com.wallet.account.dto.CreateAccountRequest;
import com.wallet.account.exception.AccountAlreadyExistsException;
import com.wallet.account.exception.AccountNotFoundException;
import com.wallet.account.mapper.AccountMapper;
import com.wallet.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional(readOnly = true)
    public AccountResponse getMyAccount(String ownerUsername) {
        log.debug("Fetching account for user: {}", ownerUsername);

        Account account = accountRepository.findByOwnerUsername(ownerUsername)
                .orElseThrow(() -> new AccountNotFoundException(ownerUsername));

        return accountMapper.toResponse(account);
    }

    @Transactional
    public AccountResponse createAccount(String ownerUsername, CreateAccountRequest request) {
        log.info("Creating account for user: {} with currency: {}", ownerUsername, request.currency());

        if (accountRepository.findByOwnerUsername(ownerUsername).isPresent()) {
            throw new AccountAlreadyExistsException(ownerUsername);
        }

        Account account = new Account(ownerUsername, request.currency());
        Account savedAccount = accountRepository.save(account);

        log.info("Account created with id: {} for user: {}", savedAccount.getId(), ownerUsername);

        return accountMapper.toResponse(savedAccount);
    }
}
