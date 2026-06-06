package com.wallet.account.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventConsumer {

    @KafkaListener(topics = "account-events", groupId = "account-service")
    public void handle(TransactionRecordedEvent event) {
        log.info("CONSUMED event: account={}, type={}, amount={}, balanceAfter={}",
                event.accountId(), event.type(), event.amount(), event.balanceAfter());
    }
}
