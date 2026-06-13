package com.wallet.transaction.service;

import com.wallet.transaction.domain.Transfer;
import com.wallet.transaction.domain.TransferStatus;
import com.wallet.transaction.event.DebitResult;
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

    @Transactional
    public void onDebitResult(DebitResult result) {
        Transfer transfer = transferRepository.findById(result.transferId())
                .orElse(null);

        if (transfer == null) {
            log.warn("Received DebitResult for unknown transfer {}", result.transferId());
            return;
        }

        if (transfer.getStatus() != TransferStatus.PENDING) {
            log.info("Transfer {} already processed (status={}), ignoring duplicate result", transfer.getId(), transfer.getStatus());
            return;
        }

        if (result.success()) {
            transfer.markDebited();
            log.info("Transfer {} moved to DEBITED state", transfer.getId());
        } else {
            transfer.markFailed(result.failureReason());
            log.info("Transfer {} moved to FAILED: {}", transfer.getId(), result.failureReason());
        }
    }
}
