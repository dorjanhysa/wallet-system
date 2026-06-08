package com.wallet.account.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.account.domain.OutboxEvent;
import com.wallet.account.domain.TransactionType;
import com.wallet.account.event.TransactionRecordedEvent;
import com.wallet.account.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxWriterTest {

    @Mock
    private OutboxEventRepository outboxRepository;

    @Captor
    private ArgumentCaptor<OutboxEvent> outboxCaptor;

    private OutboxWriter outboxWriter;

    @BeforeEach
    void setUp() {
        outboxWriter = new OutboxWriter(outboxRepository, new ObjectMapper());
    }

    @Test
    void write_serializesPayloadAndSavesOutboxEvent() {
        UUID accountId = UUID.randomUUID();
        TransactionRecordedEvent event = new TransactionRecordedEvent(
                accountId, "dorjan", TransactionType.DEPOSIT,
                new BigDecimal("100.00"), new BigDecimal("100.00"));

        outboxWriter.write("Account", accountId, "TransactionRecorded", event);

        verify(outboxRepository).save(outboxCaptor.capture());
        OutboxEvent saved = outboxCaptor.getValue();

        assertThat(saved.getAggregateType()).isEqualTo("Account");
        assertThat(saved.getAggregateId()).isEqualTo(accountId);
        assertThat(saved.getEventType()).isEqualTo("TransactionRecorded");
        assertThat(saved.isPublished()).isFalse();
        assertThat(saved.getPayload()).contains("dorjan").contains("DEPOSIT").contains("100.00");
    }
}
