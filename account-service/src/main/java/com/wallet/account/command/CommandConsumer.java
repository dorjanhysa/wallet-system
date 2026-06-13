package com.wallet.account.command;

import com.wallet.account.event.CompensateCommand;
import com.wallet.account.event.CreditCommand;
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
            topics = "account-debit-commands",
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

    @KafkaListener(
            topics = "account-credit-commands",
            groupId = "account-service-commads",
            properties = {
                    "spring.json.value.default.type=com.wallet.account.event.CreditCommand",
                    "spring.json.use.type.headers=false"
            }
    )
    public void handleCredit(CreditCommand command) {
        log.info("Received CreditCommand for transfer {} on account {}", command.transferId(), command.accountId());
        commandHandler.handleCredit(command);
    }

    @KafkaListener(
            topics = "account-compensate-commands",
            groupId = "account-service-commands",
            properties = {
                    "spring.json.value.default.type=com.wallet.account.event.CompensateCommand",
                    "spring.json.use.type.headers=false"
            }
    )
    public void handleCompensate(CompensateCommand command) {
        log.info("Received CompensateCommand for transfer {} on account {}",
                command.transferId(), command.accountId());
        commandHandler.handleCompensate(command);
    }
}
