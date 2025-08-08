package com.stofina.app.orderservice.exception;

import lombok.Getter;

@Getter
public class InvalidOrderException extends RuntimeException {

    private final String errorCode;
    private final String field;

    public InvalidOrderException(String message, String errorCode, String field) {
        super(message);
        this.errorCode = errorCode;
        this.field = field;
    }
}