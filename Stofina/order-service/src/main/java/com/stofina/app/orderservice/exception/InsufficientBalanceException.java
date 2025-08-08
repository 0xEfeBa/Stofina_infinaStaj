package com.stofina.app.orderservice.exception;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InsufficientBalanceException extends RuntimeException {

    private final BigDecimal required;
    private final BigDecimal available;

    public InsufficientBalanceException(String message, BigDecimal required, BigDecimal available) {
        super(message);
        this.required = required;
        this.available = available;
    }
}
