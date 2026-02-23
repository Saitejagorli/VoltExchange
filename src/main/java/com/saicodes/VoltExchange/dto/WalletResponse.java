package com.saicodes.VoltExchange.dto;

import com.saicodes.VoltExchange.entities.Wallet;
import com.saicodes.VoltExchange.enums.WalletStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        BigDecimal balance,
        WalletStatus status,
        LocalDateTime createdAt
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getStatus(),
                wallet.getCreatedAt()
        );
    }
}
