package com.stofina.app.mailservice.exception;

import com.stofina.app.commondata.exception.ApiException;
import org.springframework.http.HttpStatus;


public class EmailConnectionException extends ApiException {

    public EmailConnectionException(String message, HttpStatus httpStatus) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE) ;
    }
}
