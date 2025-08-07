package com.stofina.app.market_data_service.exception;

public class StockNotFoundException  extends RuntimeException {
    public StockNotFoundException(String message) {
        super(message);
    }
}
