package com.stofina.app.portfolioservice.exception;

import com.stofina.app.commondata.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InactiveAccountOperationException extends ApiException {
    public InactiveAccountOperationException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
