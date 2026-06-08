package com.wallet.account.outbox;

import com.wallet.account.domain.OutboxEvent;
import com.wallet.account.event.EventPublisher;
import com.wallet.account.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository outboxEventRepository;
    private final EventPublisher eventPublisher;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository
                .findByPublishedFalseOrderByCreatedAtAsc(PageRequest.of(0, BATCH_SIZE));

        if (pending.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to publish", pending.size());

        for (OutboxEvent event : pending) {
            try {
                eventPublisher.publishRaw(
                        event.getId().toString(),
                        event.getAggregateId().toString(),
                        event.getPayload()
                );
                event.markAsPublished();
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}
