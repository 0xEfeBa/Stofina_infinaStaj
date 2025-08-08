package com.stofina.app.marketdataservice.exceptions;

public class MarketClosedException extends RuntimeException {
    public MarketClosedException(String message) {
        super(message);
    }
}