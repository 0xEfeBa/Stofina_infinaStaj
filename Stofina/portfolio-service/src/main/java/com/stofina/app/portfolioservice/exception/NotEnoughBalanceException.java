package com.stofina.app.portfolioservice.exception;

import com.stofina.app.commondata.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotEnoughBalanceException extends ApiException {
    public NotEnoughBalanceException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
