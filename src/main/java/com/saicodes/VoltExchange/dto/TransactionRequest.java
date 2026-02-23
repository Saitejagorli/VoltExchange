package com.saicodes.VoltExchange.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest (
        @NotNull(message = "Receiver email is required")
        String receiverEmail,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount
){
}
