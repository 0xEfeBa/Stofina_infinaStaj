package com.stofina.app.portfolioservice.exception;

 import com.stofina.app.commondata.exception.ApiException;
 import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends ApiException {
    public InsufficientFundsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
