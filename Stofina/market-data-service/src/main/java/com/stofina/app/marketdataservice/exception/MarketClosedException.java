package com.stofina.app.marketdataservice.exception;

public class MarketClosedException extends RuntimeException {
    public MarketClosedException(String message) {
        super(message);
    }
}