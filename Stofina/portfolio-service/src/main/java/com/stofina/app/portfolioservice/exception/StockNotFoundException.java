package com.stofina.app.portfolioservice.exception;


import com.stofina.app.commondata.exception.ApiException;
import org.springframework.http.HttpStatus;

public class StockNotFoundException extends ApiException {
    public StockNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
