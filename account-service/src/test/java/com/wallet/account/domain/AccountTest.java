package com.wallet.account.domain;

import com.wallet.account.exception.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Test
    void newAccount_startsWithZeroBalance() {
        Account account = new Account("dorjan", "EUR");

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.getCurrency()).isEqualTo("EUR");
        assertThat(account.getOwnerUsername()).isEqualTo("dorjan");
    }

    @Test
    void credit_increasesBalance() {
        Account account = new Account("dorjan", "EUR");

        account.credit(new BigDecimal("100.00"));

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void debit_decreasesBalance() {
        Account account = new Account("dorjan", "EUR");
        account.credit(new BigDecimal("100.00"));

        account.debit(new BigDecimal("30.00"));

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    void debit_withInsufficientFunds_throwsException() {
        Account account = new Account("dorjan", "EUR");
        account.credit(new BigDecimal("50.00"));

        assertThatThrownBy(() -> account.debit(new BigDecimal("100.00")))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void credit_withNegativeAmount_throwsException() {
        Account account = new Account("dorjan", "EUR");

        assertThatThrownBy(() -> account.credit(new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debit_withZeroAmount_throwsException() {
        Account account = new Account("dorjan", "EUR");
        account.credit(new BigDecimal("100.00"));

        assertThatThrownBy(() -> account.debit(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}