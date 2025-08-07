package com.stofina.app.orderservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Account ID cannot be null")
    private Long accountId;

    @NotBlank(message = "Symbol cannot be blank")
    private String symbol;

    @NotBlank(message = "Order type cannot be blank")
    private String orderType;

    @NotBlank(message = "Side cannot be blank")
    @Pattern(regexp = "BUY|SELL", message = "Side must be either BUY or SELL")
    private String side;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    @Positive(message = "Stop price must be greater than 0")
    private BigDecimal stopPrice;

    @NotBlank(message = "Time in force is required")
    private String timeInForce;

    private LocalDateTime expiryDate;

    private String clientOrderId;
}
