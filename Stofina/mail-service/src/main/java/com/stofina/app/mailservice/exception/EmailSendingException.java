package com.stofina.app.mailservice.exception;

import com.stofina.app.commondata.exception.ApiException;
import org.springframework.http.HttpStatus;

 public class EmailSendingException extends ApiException {
    public EmailSendingException(String message) {
        super(message,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
