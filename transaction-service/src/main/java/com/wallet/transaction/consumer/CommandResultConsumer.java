package com.wallet.transaction.consumer;

import com.wallet.transaction.event.CommandResult;
import com.wallet.transaction.service.TransferOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandResultConsumer {

    private final TransferOrchestrator orchestrator;

    @KafkaListener(
            topics = "account-command-results",
            groupId = "transaction-service",
            properties = {
                    "spring.json.value.default.type=com.wallet.transaction.event.CommandResult",
                    "spring.json.use.type.headers=false"
            }
    )
    public void handleResult(CommandResult result) {
        log.info("Received CommandResult for transfer {}: step={}, success={}",
                result.transferId(), result.step(), result.success());
        orchestrator.onCommandResult(result);
    }
}
