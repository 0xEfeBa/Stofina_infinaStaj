package com.stofina.app.marketdataservice.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ServiceResult<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ServiceResult<T> success(T data, String message) {
        return ServiceResult.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ServiceResult<T> failure(String message) {
        return ServiceResult.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
