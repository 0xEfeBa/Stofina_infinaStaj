package com.stofina.app.orderservice.common;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResult<T>  {
    private boolean success;
    private String message;
    private T data;

    public static <T> ServiceResult<T> success(T data) {
        return ServiceResult.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ServiceResult<T> success(T data, String message) {
        return ServiceResult.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    public static <T> ServiceResult<T> failure(String message) {
        return ServiceResult.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
    public HttpStatus getHttpStatus() {
        return success ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
    }
}
