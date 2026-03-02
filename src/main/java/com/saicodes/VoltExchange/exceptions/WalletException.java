package com.saicodes.VoltExchange.exceptions;

import org.springframework.http.HttpStatus;

public class WalletException extends RuntimeException {
    private final HttpStatus status;
    public WalletException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
