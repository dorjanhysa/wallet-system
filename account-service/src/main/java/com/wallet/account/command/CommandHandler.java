package com.wallet.account.command;

import com.wallet.account.domain.Account;
import com.wallet.account.domain.ProcessedCommand;
import com.wallet.account.domain.Transaction;
import com.wallet.account.domain.TransactionType;
import com.wallet.account.event.DebitCommand;
import com.wallet.account.event.DebitResult;
import com.wallet.account.exception.InsufficientFundsException;
import com.wallet.account.outbox.OutboxWriter;
import com.wallet.account.repository.AccountRepository;
import com.wallet.account.repository.ProcessedCommandRepository;
import com.wallet.account.repository.TransactionRepository;
import io.lettuce.core.dynamic.annotation.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandler {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ProcessedCommandRepository processedCommandRepository;
    private final OutboxWriter outboxWriter;

    @Transactional
    public void handleDebit(DebitCommand command) {
        if (processedCommandRepository.existsById(command.transferId())) {
            log.info("Command for transfer {} already processed, skipping", command.transferId());
            return;
        }

        Optional<Account> accountOpt = accountRepository.findById(command.accountId());
        if (accountOpt.isEmpty() || !accountOpt.get().getOwnerUsername().equals(command.ownerUsername())) {
            log.warn("Debit failed for transfer {}: account not found or not owned", command.transferId());
            emitResult(command, false, "Account not found or not owned");
            markProcessed(command, "DebitFailed");
            return;
        }

        Account account = accountOpt.get();

        try {
            account.debit(command.amount());
        } catch (InsufficientFundsException e) {
            log.warn("Debit failed for transfer {}: insufficient funds", command.transferId());
            emitResult(command, false, "Insufficient funds");
            markProcessed(command, "DebitFailed");
            return;
        }

        Transaction tx = new Transaction(
                account.getId(),
                TransactionType.WITHDRAWAL,
                command.amount(),
                account.getBalance());
        transactionRepository.save(tx);

        emitResult(command, true, null);
        markProcessed(command, "DebitSucceeded");

        log.info("Debit succeeded for transfer {} on account {}", command.transferId(), command.accountId());
    }

    private void emitResult(DebitCommand command, boolean success, String failureReason) {
        outboxWriter.write(
                "Account",
                command.accountId(),
                "DebitResult",
                new DebitResult(command.transferId(), command.accountId(), success, failureReason));
    }

    private void markProcessed(DebitCommand command, String resultType) {
        processedCommandRepository.save(new ProcessedCommand(command.transferId(), resultType));
    }
}
