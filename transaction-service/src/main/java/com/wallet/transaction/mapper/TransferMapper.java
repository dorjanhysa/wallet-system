package com.wallet.transaction.mapper;

import com.wallet.transaction.domain.Transfer;
import com.wallet.transaction.dto.TransferResponse;
import org.springframework.stereotype.Component;

@Component
public class TransferMapper {

    public TransferResponse toTransferResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getFromAccountId(),
                transfer.getToAccountId(),
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getFailureReason(),
                transfer.getCreatedAt());
    }
}
