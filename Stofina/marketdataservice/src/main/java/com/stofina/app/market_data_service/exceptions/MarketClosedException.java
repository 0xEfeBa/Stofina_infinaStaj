package com.stofina.market_data_service.exception;

public class MarketClosedException extends RuntimeException {
    public MarketClosedException(String message) {
        super(message);
    }
}