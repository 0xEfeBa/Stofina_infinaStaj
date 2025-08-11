package com.stofina.app.portfolioservice.request.stock;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellStockRequest {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotBlank(message = "Stock symbol is required")
    private String symbol;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotNull(message = "Price per unit is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    private String description;
}
