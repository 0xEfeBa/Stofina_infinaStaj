package com.stofina.app.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ValidationErrorResponse {
    private String field;
    private String message;
    private Object rejectedValue;
}
