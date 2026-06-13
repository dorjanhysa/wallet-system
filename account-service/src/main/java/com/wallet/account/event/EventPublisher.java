package com.wallet.account.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    public static final String EVENT_ID_HEADER = "event-id";

    private static final Map<String, String> TOPIC_ROUTING = Map.of(
            "TransactionRecorded", "account-events",
            "DebitResult", "account-command-results"
    );

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRaw(String eventId, String eventType, String key, String jsonPayload) {
        String topic = TOPIC_ROUTING.get(eventType);
        if (topic == null) {
            throw new IllegalStateException("No topic mapping for event type: " + eventType);
        }

        log.debug("Publishing event type '{}' to topic '{}', eventId: {}", eventType, topic, eventId);

        ProducerRecord<String, Object> eventRecord = new ProducerRecord<>(topic, key, jsonPayload);
        eventRecord.headers().add(new RecordHeader(
                EVENT_ID_HEADER, eventId.getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(eventRecord);
    }
}
