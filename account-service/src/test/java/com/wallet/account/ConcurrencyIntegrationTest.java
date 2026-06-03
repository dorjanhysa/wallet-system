package com.wallet.account;

import com.wallet.account.domain.Account;
import com.wallet.account.repository.AccountRepository;
import com.wallet.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ConcurrencyIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    private static final String USER = "concurrent-user";

    @BeforeEach
    void setup() {
        accountRepository.deleteAll();
        Account account = new Account(USER, "EUR");
        account.credit(new BigDecimal("100.00"));
        accountRepository.save(account);
    }

    @Test
    void concurrentWithdrawals_onlyOneSucceeds_dueToOptimisticLocking() throws InterruptedException {
        int threads = 2;
        BigDecimal withdrawAmount = new BigDecimal("80.00");

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    accountService.withdraw(USER, withdrawAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    conflictCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(1);

        Account finalAccount = accountRepository.findByOwnerUsername(USER).orElseThrow();
        assertThat(finalAccount.getBalance()).isEqualByComparingTo(new BigDecimal("20.00"));
    }
}
