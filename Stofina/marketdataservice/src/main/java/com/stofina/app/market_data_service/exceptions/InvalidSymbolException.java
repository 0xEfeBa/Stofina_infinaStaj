package com.stofina.market_data_service.exception;

public class InvalidSymbolException extends RuntimeException {
    public InvalidSymbolException(String message) {
        super(message);
    }
    
    public InvalidSymbolException(String symbol, String message) {
        super(String.format("Invalid symbol '%s': %s", symbol, message));
    }
}