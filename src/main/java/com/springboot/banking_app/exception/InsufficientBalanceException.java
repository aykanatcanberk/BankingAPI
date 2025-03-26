package com.springboot.banking_app.exception;

public class InsufficientBalanceException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Not enough balance";

    public InsufficientBalanceException() {
        super(DEFAULT_MESSAGE);
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}