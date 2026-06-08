package com.wallet.account;

import com.wallet.account.domain.Account;
import com.wallet.account.repository.AccountRepository;
import com.wallet.account.repository.OutboxEventRepository;
import com.wallet.account.service.AccountService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
class OutboxIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OutboxEventRepository outboxRepository;

    private static final String USER = "outbox-user";

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        accountRepository.deleteAll();
        accountRepository.save(new Account(USER, "EUR"));
    }

    @Test
    void deposit_writesOutboxEvent_andPollerPublishesIt() {
        accountService.deposit(USER, new BigDecimal("100.00"));

        assertThat(outboxRepository.findAll()).hasSize(1);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        verify(kafkaTemplate).send(eq("account-events"), any(), any())
                );

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var events = outboxRepository.findAll();
                    assertThat(events).hasSize(1);
                    assertThat(events.getFirst().isPublished()).isTrue();
                });
    }
}
