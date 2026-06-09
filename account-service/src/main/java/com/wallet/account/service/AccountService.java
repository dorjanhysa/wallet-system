package com.wallet.account.service;

import com.wallet.account.domain.Account;
import com.wallet.account.domain.Transaction;
import com.wallet.account.domain.TransactionType;
import com.wallet.account.dto.AccountResponse;
import com.wallet.account.dto.CreateAccountRequest;
import com.wallet.account.dto.TransactionResponse;
import com.wallet.account.event.TransactionRecordedEvent;
import com.wallet.account.exception.AccountNotFoundException;
import com.wallet.account.mapper.AccountMapper;
import com.wallet.account.outbox.OutboxWriter;
import com.wallet.account.repository.AccountRepository;
import com.wallet.account.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final TransactionRepository transactionRepository;
    private final OutboxWriter outboxWriter;

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(String ownerUsername) {
        log.debug("Fetching all accounts for user: {}", ownerUsername);
        return accountRepository.findAllByOwnerUsername(ownerUsername).stream()
                .map(accountMapper::toAccountResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID accountId, String ownerUsername) {
        Account account = getOwnedAccount(accountId, ownerUsername);
        return accountMapper.toAccountResponse(account);
    }

    @Transactional
    public AccountResponse createAccount(String ownerUsername, CreateAccountRequest request) {
        log.info("Creating account for user: {} with currency: {}", ownerUsername, request.currency());

        Account account = new Account(ownerUsername, request.currency());
        Account savedAccount = accountRepository.save(account);

        log.info("Account created with id: {} for user: {}", savedAccount.getId(), ownerUsername);

        return accountMapper.toAccountResponse(savedAccount);
    }

    @Transactional
    public AccountResponse deposit(UUID accountId, String ownerUsername, BigDecimal amount) {
        log.info("Deposit of {} requested on account {} by user: {}", amount, accountId, ownerUsername);

        Account account = getOwnedAccount(accountId, ownerUsername);

        account.credit(amount);

        Transaction tx = new Transaction(
                account.getId(),
                TransactionType.DEPOSIT,
                amount,
                account.getBalance()
        );
        transactionRepository.save(tx);

        outboxWriter.write(
                "Account",
                account.getId(),
                "TransactionRecorded",
                new TransactionRecordedEvent(account.getId(), ownerUsername, TransactionType.DEPOSIT, amount, account.getBalance())
        );

        log.info("Deposit completed on account {}", accountId);
        return accountMapper.toAccountResponse(account);
    }

    @Transactional
    public AccountResponse withdraw(UUID accountId, String ownerUsername, BigDecimal amount) {
        log.info("Withdraw of {} requested on account {} by user: {}", amount, accountId, ownerUsername);

        Account account = getOwnedAccount(accountId, ownerUsername);

        account.debit(amount);

        Transaction tx = new Transaction(
                account.getId(),
                TransactionType.WITHDRAWAL,
                amount,
                account.getBalance()
        );
        transactionRepository.save(tx);

        outboxWriter.write(
                "Account",
                account.getId(),
                "TransactionRecorded",
                new TransactionRecordedEvent(account.getId(), ownerUsername, TransactionType.WITHDRAWAL, amount, account.getBalance())
        );

        log.info("Withdraw completed on account {}", accountId);
        return accountMapper.toAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getMyTransactions(UUID accountId, String ownerUsername, Pageable pageable) {
        Account account = getOwnedAccount(accountId, ownerUsername);
        return transactionRepository
                .findByAccountIdOrderByCreatedAtDesc(account.getId(), pageable)
                .map(accountMapper::toTransactionResponse);
    }

    private Account getOwnedAccount(UUID accountId, String ownerUsername) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (!account.getOwnerUsername().equals(ownerUsername)) {
            log.warn("User '{}' attempted to access account {} owned by another user",
                    ownerUsername, accountId);
            throw new AccountNotFoundException(accountId);
        }

        return account;
    }
}
