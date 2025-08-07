package com.stofina.app.orderservice.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarketDataException extends RuntimeException {

    private String errorCode;
    private boolean retryable;

    public MarketDataException(String message, String errorCode, boolean retryable) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    public MarketDataException(String message, Throwable cause, String errorCode, boolean retryable) {
        super(message, cause);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

}
