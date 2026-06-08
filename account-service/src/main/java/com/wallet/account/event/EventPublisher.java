package com.wallet.account.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private static final String TOPIC = "account-events";
    public static final String EVENT_ID_HEADER = "event-id";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRaw(String eventId, String key, String jsonPayload) {
        log.debug("Publishing event to Kafka, eventId: {}, key: {}", eventId, key);

        ProducerRecord<String, Object> eventRecord = new ProducerRecord<>(TOPIC, key, jsonPayload);
        eventRecord.headers().add(new RecordHeader(
                EVENT_ID_HEADER, eventId.getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(eventRecord);
    }
}
