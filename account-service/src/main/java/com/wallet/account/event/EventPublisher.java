package com.wallet.account.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private static final String TOPIC = "account-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRaw(String key, String jsonPayload) {
        log.debug("Publishing event to Kafka, key: {}", key);
        kafkaTemplate.send(TOPIC, key, jsonPayload);
    }
}
