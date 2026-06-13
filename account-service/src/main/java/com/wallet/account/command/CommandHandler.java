package com.wallet.account.command;

import com.wallet.account.domain.*;
import com.wallet.account.event.CommandResult;
import com.wallet.account.event.CompensateCommand;
import com.wallet.account.event.CreditCommand;
import com.wallet.account.event.DebitCommand;
import com.wallet.account.exception.InsufficientFundsException;
import com.wallet.account.outbox.OutboxWriter;
import com.wallet.account.repository.AccountRepository;
import com.wallet.account.repository.ProcessedCommandRepository;
import com.wallet.account.repository.TransactionRepository;
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
        ProcessedCommandId id = new ProcessedCommandId(command.transferId(), "DEBIT");
        if (processedCommandRepository.existsById(id)) {
            log.info("Debit for transfer {} already processed, skipping", command.transferId());
            return;
        }

        Optional<Account> accountOpt = accountRepository.findById(command.accountId());
        if (accountOpt.isEmpty() || !accountOpt.get().getOwnerUsername().equals(command.ownerUsername())) {
            log.warn("Debit failed for transfer {}: account not found or not owned", command.transferId());
            emitDebitResult(command, false, "Account not found or not owned");
            markDebitProcessed(command, "DebitFailed");
            return;
        }

        Account account = accountOpt.get();

        try {
            account.debit(command.amount());
        } catch (InsufficientFundsException e) {
            log.warn("Debit failed for transfer {}: insufficient funds", command.transferId());
            emitDebitResult(command, false, "Insufficient funds");
            markDebitProcessed(command, "DebitFailed");
            return;
        }

        Transaction tx = new Transaction(
                account.getId(),
                TransactionType.WITHDRAWAL,
                command.amount(),
                account.getBalance());
        transactionRepository.save(tx);

        emitDebitResult(command, true, null);
        markDebitProcessed(command, "DebitSucceeded");

        log.info("Debit succeeded for transfer {} on account {}", command.transferId(), command.accountId());
    }

    @Transactional
    public void handleCredit(CreditCommand command) {
        ProcessedCommandId id = new ProcessedCommandId(command.transferId(), "CREDIT");
        if (processedCommandRepository.existsById(id)) {
            log.info("Credit for transfer {} already processed, skipping", command.transferId());
            return;
        }

        Optional<Account> accountOpt = accountRepository.findById(command.accountId());
        if (accountOpt.isEmpty()) {
            log.warn("Credit failed for transfer {}: destination account not found", command.transferId());
            emitCreditResult(command, false, "Destination account not found");
            markCreditProcessed(command, "CreditFailed");
            return;
        }

        Account account = accountOpt.get();
        account.credit(command.amount());

        Transaction tx = new Transaction(
                account.getId(),
                TransactionType.DEPOSIT,
                command.amount(),
                account.getBalance());
        transactionRepository.save(tx);

        emitCreditResult(command, true, null);
        markCreditProcessed(command, "CreditSucceeded");
        log.info("Credit succeeded for transfer {} on account {}", command.transferId(), command.accountId());
    }

    @Transactional
    public void handleCompensate(CompensateCommand command) {
        ProcessedCommandId id = new ProcessedCommandId(command.transferId(), "COMPENSATE");
        if (processedCommandRepository.existsById(id)) {
            log.info("Compensation for transfer {} already processed, skipping", command.transferId());
            return;
        }

        Optional<Account> accountOpt = accountRepository.findById(command.accountId());
        if (accountOpt.isEmpty()) {
            log.error("Compensation failed for transfer {}: source account not found", command.transferId());
            emitCompensateResult(command, false, "Source account not found");
            processedCommandRepository.save(new ProcessedCommand(command.transferId(), "COMPENSATE", "CompensateFailed"));
            return;
        }

        Account account = accountOpt.get();
        account.credit(command.amount());

        Transaction tx = new Transaction(
                account.getId(), TransactionType.DEPOSIT, command.amount(), account.getBalance());
        transactionRepository.save(tx);

        emitCompensateResult(command, true, null);
        processedCommandRepository.save(new ProcessedCommand(command.transferId(), "COMPENSATE", "CompensateSucceeded"));
        log.info("Compensation succeeded for transfer {}: account {} restored", command.transferId(), command.accountId());
    }

    private void emitDebitResult(DebitCommand command, boolean success, String failureReason) {
        outboxWriter.write(
                "Account",
                command.accountId(),
                "DebitResult",
                new CommandResult(command.transferId(), command.accountId(), "DEBIT", success, failureReason));
    }

    private void emitCreditResult(CreditCommand command, boolean success, String failureReason) {
        outboxWriter.write(
                "Account",
                command.accountId(),
                "CreditResult",
                new CommandResult(command.transferId(), command.accountId(), "CREDIT", success, failureReason));
    }

    private void emitCompensateResult(CompensateCommand command, boolean success, String failureReason) {
        outboxWriter.write(
                "Account",
                command.accountId(), 
                "CommandResult",
                new CommandResult(command.transferId(), command.accountId(), "COMPENSATE", success, failureReason));
    }

    private void markDebitProcessed(DebitCommand command, String resultType) {
        processedCommandRepository.save(
                new ProcessedCommand(command.transferId(), "DEBIT", resultType));
    }

    private void markCreditProcessed(CreditCommand command, String resultType) {
        processedCommandRepository.save(
                new ProcessedCommand(command.transferId(), "CREDIT", resultType));
    }
}
