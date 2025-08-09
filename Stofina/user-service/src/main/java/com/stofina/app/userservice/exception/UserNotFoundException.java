package com.stofina.app.userservice.exception;

 import com.stofina.app.commondata.exception.ApiException;
 import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException (String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

