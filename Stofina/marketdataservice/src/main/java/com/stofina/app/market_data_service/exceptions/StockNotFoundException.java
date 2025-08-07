package com.stofina.market_data_service.exception;


public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String message) {
        super(message);
    }
    
    public StockNotFoundException(String symbol, String message) {
        super(String.format("Stock '%s': %s", symbol, message));
    }
}