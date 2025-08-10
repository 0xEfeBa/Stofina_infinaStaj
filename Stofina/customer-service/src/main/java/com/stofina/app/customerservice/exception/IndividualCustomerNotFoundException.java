package com.stofina.app.customerservice.exception;

import com.stofina.app.commondata.exception.ApiException;
import org.springframework.http.HttpStatus;

public class IndividualCustomerNotFoundException extends ApiException {
    public IndividualCustomerNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
