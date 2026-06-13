package com.wallet.transaction.service;

import com.wallet.transaction.domain.Transfer;
import com.wallet.transaction.dto.CreateTransferRequest;
import com.wallet.transaction.dto.TransferResponse;
import com.wallet.transaction.event.DebitCommand;
import com.wallet.transaction.exception.InvalidTransferException;
import com.wallet.transaction.exception.TransferNotFoundException;
import com.wallet.transaction.mapper.TransferMapper;
import com.wallet.transaction.outbox.OutboxWriter;
import com.wallet.transaction.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final OutboxWriter outboxWriter;

    @Transactional
    public TransferResponse initiateTransfer(String ownerUsername, CreateTransferRequest request) {
        log.info("Transfer initiation requested by user '{}': {} from {} to {}", ownerUsername, request.amount(), request.fromAccountId(), request.toAccountId());

        if (request.fromAccountId().equals(request.toAccountId())) {
            throw  new InvalidTransferException("Source and destination accounts must be different");
        }

        Transfer transfer = new Transfer(
                request.fromAccountId(),
                request.toAccountId(),
                ownerUsername,
                request.amount());

        Transfer savedTransfer = transferRepository.save(transfer);

        outboxWriter.write(
                "Transfer",
                savedTransfer.getId(),
                "DebitCommand",
                new DebitCommand(
                        savedTransfer.getId(),
                        savedTransfer.getFromAccountId(),
                        ownerUsername,
                        savedTransfer.getAmount())
        );

        log.info("Transfer {} created in PENDING state, DebitCommand queued", savedTransfer.getId());

        return transferMapper.toTransferResponse(savedTransfer);
    }

    @Transactional(readOnly = true)
    public TransferResponse getTransfer(UUID transferId, String ownerUsername) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(transferId));

        if (!transfer.getOwnerUsername().equals(ownerUsername)) {
            log.warn("User '{}' attempted to access transfer {} owned by another user", ownerUsername, transferId);
            throw new TransferNotFoundException(transferId);
        }

        return transferMapper.toTransferResponse(transfer);
    }
}
