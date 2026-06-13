package com.wallet.account.repository;

import com.wallet.account.domain.ProcessedCommand;
import com.wallet.account.domain.ProcessedCommandId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedCommandRepository extends JpaRepository<ProcessedCommand, ProcessedCommandId> {
}
