package com.wallet.account.service;

import com.wallet.account.domain.Account;
import com.wallet.account.dto.AccountResponse;
import com.wallet.account.exception.AccountNotFoundException;
import com.wallet.account.mapper.AccountMapper;
import com.wallet.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    @Test
    void deposit_onExistingAccount_creditsBalance() {
        Account account = new Account("dorjan", "EUR");
        account.credit(new BigDecimal("50.00"));

        when(accountRepository.findByOwnerUsername("dorjan")).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account))
                .thenReturn(new AccountResponse(UUID.randomUUID(), "dorjan", new BigDecimal("150.00"), "EUR"));

        accountService.deposit("dorjan", new BigDecimal("100.00"));

        // verifico che il dominio sia stato chiamato: il saldo dell'entità è cambiato
        assertThatBalance(account, "150.00");
    }

    @Test
    void deposit_onNonExistentAccount_throwsNotFound() {
        when(accountRepository.findByOwnerUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deposit("ghost", new BigDecimal("100.00")))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountMapper, never()).toResponse(any());
    }

    private void assertThatBalance(Account account, String expected) {
        org.assertj.core.api.Assertions.assertThat(account.getBalance())
                .isEqualByComparingTo(new BigDecimal(expected));
    }
}
