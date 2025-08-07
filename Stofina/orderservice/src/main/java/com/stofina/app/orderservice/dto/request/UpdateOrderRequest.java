package com.stofina.app.orderservice.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateOrderRequest {

    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    @Positive(message = "Quantity must be greater than 0")
    private BigDecimal quantity;
}
