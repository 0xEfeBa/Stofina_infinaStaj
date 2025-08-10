package com.stofina.app.portfolioservice.exception;

import com.stofina.app.commondata.exception.ApiException;
import org.springframework.http.HttpStatus;

public class TransactionNotFoundException extends ApiException {
    public TransactionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
