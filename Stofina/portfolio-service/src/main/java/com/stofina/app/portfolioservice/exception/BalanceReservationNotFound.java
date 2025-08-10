package com.stofina.app.portfolioservice.exception;

import com.stofina.app.commondata.exception.ApiException;
import org.springframework.http.HttpStatus;


public class BalanceReservationNotFound extends ApiException {
    public BalanceReservationNotFound(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
