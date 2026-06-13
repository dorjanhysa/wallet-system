package com.wallet.transaction.service;

import com.wallet.transaction.domain.Transfer;
import com.wallet.transaction.domain.TransferStatus;
import com.wallet.transaction.event.CommandResult;
import com.wallet.transaction.event.CompensateCommand;
import com.wallet.transaction.event.CreditCommand;
import com.wallet.transaction.outbox.OutboxWriter;
import com.wallet.transaction.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferOrchestrator {

    private final TransferRepository transferRepository;
    private final OutboxWriter outboxWriter;

    @Transactional
    public void onCommandResult(CommandResult result) {
        Transfer transfer = transferRepository.findById(result.transferId()).orElse(null);
        if (transfer == null) {
            log.warn("Received CommandResult for unknown transfer {}", result.transferId());
            return;
        }

        switch (result.step()) {
            case "DEBIT" -> handleDebitResult(transfer, result);
            case "CREDIT" -> handleCreditResult(transfer, result);
            case "COMPENSATE" -> handleCompensateResult(transfer, result);
            default -> log.warn("Unknown step '{}' for transfer {}", result.step(), transfer.getId());
        }
    }

    private void handleDebitResult(Transfer transfer, CommandResult result) {
        if (transfer.getStatus() != TransferStatus.PENDING) {
            log.info("Transfer {} not in PENDING (status={}), ignoring debit result",
                    transfer.getId(), transfer.getStatus());
            return;
        }

        if (result.success()) {
            transfer.markDebited();
            log.info("Transfer {} moved to DEBITED, queuing CreditCommand", transfer.getId());
            outboxWriter.write("Transfer", transfer.getId(), "CreditCommand",
                    new CreditCommand(transfer.getId(), transfer.getToAccountId(), transfer.getAmount()));
        } else {
            transfer.markFailed(result.failureReason());
            log.info("Transfer {} moved to FAILED: {}", transfer.getId(), result.failureReason());
        }
    }

    private void handleCreditResult(Transfer transfer, CommandResult result) {
        if (transfer.getStatus() != TransferStatus.DEBITED) {
            log.info("Transfer {} not in DEBITED (status={}), ignoring credit result",
                    transfer.getId(), transfer.getStatus());
            return;
        }

        if (result.success()) {
            transfer.markCompleted();
            log.info("Transfer {} COMPLETED successfully", transfer.getId());
        } else {
            transfer.markCompensating();
            log.warn("Transfer {} credit failed, moving to COMPENSATING: {}",
                    transfer.getId(), result.failureReason());
            outboxWriter.write(
                    "Transfer",
                    transfer.getId(),
                    "CompensateCommand",
                    new CompensateCommand(transfer.getId(), transfer.getFromAccountId(), transfer.getAmount())
            );
        }
    }

    private void handleCompensateResult(Transfer transfer, CommandResult result) {
        if (transfer.getStatus() != TransferStatus.COMPENSATING) {
            log.info("Transfer {} not in COMPENSATING (status={}), ignoring compensate result",
                    transfer.getId(), transfer.getStatus());
            return;
        }
        if (result.success()) {
            transfer.markCompensated();
            log.info("Transfer {} COMPENSATED: source account restored", transfer.getId());
        } else {
            log.error("CRITICAL: compensation failed for transfer {}: {}",
                    transfer.getId(), result.failureReason());
        }
    }
}
