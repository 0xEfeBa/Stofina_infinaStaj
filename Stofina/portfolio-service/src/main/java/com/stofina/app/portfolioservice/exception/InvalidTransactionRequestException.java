package com.stofina.app.portfolioservice.exception;

import com.stofina.app.commondata.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidTransactionRequestException extends ApiException {
    public InvalidTransactionRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
