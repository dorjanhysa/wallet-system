package com.wallet.notification.consumer;

import com.wallet.notification.event.TransactionRecordedEvent;
import com.wallet.notification.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final IdempotencyService idempotencyService;

    @KafkaListener(topics = "account-events", groupId = "notification-service")
    public void handle(
            @Payload TransactionRecordedEvent event,
            @Header(name = "event-id", required = false) String eventId) {

        if (eventId != null && !idempotencyService.markIfNew(eventId)) {
            log.info("Duplicate event {} ignored (already processed)", eventId);
            return;
        }

        log.info("Notification for user '{}': {} of {} processed. New balance: {}",
                event.ownerUsername(),
                event.type(),
                event.amount(),
                event.balanceAfter());

        sendNotification(event);
    }

    private void sendNotification(TransactionRecordedEvent event) {
        log.info("📧 Sending notification to user '{}' about their {} transaction",
                event.ownerUsername(), event.type());
    }
}
