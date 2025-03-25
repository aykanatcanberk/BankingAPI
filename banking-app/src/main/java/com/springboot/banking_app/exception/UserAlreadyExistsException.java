package com.springboot.banking_app.exception;

public class UserAlreadyExistsException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Account already exists!";

    public UserAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
}