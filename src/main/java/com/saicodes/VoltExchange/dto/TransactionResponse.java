package com.saicodes.VoltExchange.dto;

import com.saicodes.VoltExchange.entities.Transaction;
import com.saicodes.VoltExchange.enums.TransactionStatus;
import com.saicodes.VoltExchange.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID transactionId,
        String senderEmail,
        String receiverEmail,
        BigDecimal amount,
        TransactionStatus status,
        TransactionType transactionType,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getSenderWallet().getUser().getEmail(),
                transaction.getReceiverWallet().getUser().getEmail(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getType(),
                transaction.getCreatedAt()
        );

    }
}
