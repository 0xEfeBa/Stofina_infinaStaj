package com.stofina.app.userservice.exception;

import com.stofina.app.commondata.exception.ApiException;
import org.springframework.http.HttpStatus;

public class PasswordMistMatchException extends ApiException {

    private static final long serialVersionUID = 1L;

    public PasswordMistMatchException( String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}