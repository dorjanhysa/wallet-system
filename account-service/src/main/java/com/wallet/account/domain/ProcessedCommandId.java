package com.wallet.account.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ProcessedCommandId implements Serializable {

    private UUID transferId;
    private String commandType;

    public ProcessedCommandId() {}

    public ProcessedCommandId(UUID transferId, String commandType) {
        this.transferId = transferId;
        this.commandType = commandType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcessedCommandId that)) return false;
        return Objects.equals(transferId, that.transferId)
                && Objects.equals(commandType, that.commandType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transferId, commandType);
    }
}
