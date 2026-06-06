package com.wallet.notification.consumer;

import com.wallet.notification.event.TransactionRecordedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

    @KafkaListener(topics = "account-events", groupId = "notification-service")
    public void handle(TransactionRecordedEvent event) {
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
