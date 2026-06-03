package com.wallet.account.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(

        UUID id,
        String ownerUsername,
        BigDecimal balance,
        String currency
) {}
