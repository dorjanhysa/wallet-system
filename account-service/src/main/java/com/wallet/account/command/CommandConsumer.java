package com.wallet.account.command;

import com.wallet.account.event.DebitCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandConsumer {

    private final CommandHandler commandHandler;

    @KafkaListener(
            topics = "account-commands",
            groupId = "account-service-commands",
            properties = {
                    "spring.json.value.default.type=com.wallet.account.event.DebitCommand",
                    "spring.json.use.type.headers=false"
            }
    )
    public void handleDebit(DebitCommand command) {
        log.info("Received DebitCommand for transfer {} on account {}",
                command.transferId(), command.accountId());
        commandHandler.handleDebit(command);
    }
}
