package com.wallet.account.service;

import com.wallet.account.domain.Account;
import com.wallet.account.dto.AccountResponse;
import com.wallet.account.exception.AccountNotFoundException;
import com.wallet.account.mapper.AccountMapper;
import com.wallet.account.outbox.OutboxWriter;
import com.wallet.account.repository.AccountRepository;
import com.wallet.account.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private OutboxWriter outboxWriter;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    @Test
    void deposit_onExistingAccount_creditsBalance() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account("dorjan", "EUR");
        account.credit(new BigDecimal("50.00"));
        ReflectionTestUtils.setField(account, "id", accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountMapper.toAccountResponse(account))
                .thenReturn(new AccountResponse(UUID.randomUUID(), "dorjan", new BigDecimal("150.00"), "EUR"));

        accountService.deposit(accountId, "dorjan", new BigDecimal("100.00"));

        assertThatBalance(account, "150.00");
    }

    @Test
    void deposit_onNonExistentAccount_throwsNotFound() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deposit(accountId, "ghost", new BigDecimal("100.00")))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountMapper, never()).toAccountResponse(any());
    }

    private void assertThatBalance(Account account, String expected) {
        org.assertj.core.api.Assertions.assertThat(account.getBalance())
                .isEqualByComparingTo(new BigDecimal(expected));
    }

    @Test
    void deposit_onAccountOwnedByAnotherUser_throwsNotFound() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account("alice", "EUR");
        ReflectionTestUtils.setField(account, "id", accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.deposit(accountId, "dorjan", new BigDecimal("100.00")))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountMapper, never()).toAccountResponse(any(Account.class));
    }
}
