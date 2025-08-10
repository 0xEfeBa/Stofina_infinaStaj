package com.stofina.app.mailservice.exception.handler;




import com.stofina.app.commondata.response.ErrorResponse;
import com.stofina.app.mailservice.exception.EmailSendingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice

public class GlobalExceptionHandler {
    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendingException(EmailSendingException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(exception.getMessage())
                .exceptionType(exception.getClass().getSimpleName())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()) // Or exception.getStatus().value() if status is variable
                .errorTime(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
