package com.stofina.app.orderservice.dto.portfolio;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for Portfolio Service buy stock operation.
 * Used when Order Service needs to reserve balance for buy orders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "orderId")
public class BuyStockRequest {

    @NotNull(message = "Account ID cannot be null")
    @Positive(message = "Account ID must be positive")
    private Long accountId;

    @NotBlank(message = "Symbol cannot be blank")
    @Pattern(regexp = "^[A-Z]{4,6}$", message = "Symbol must be 4-6 uppercase letters")
    private String symbol;

    @NotNull(message = "Order ID cannot be null")
    @Positive(message = "Order ID must be positive")
    private Long orderId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0.01")
    @Digits(integer = 10, fraction = 4, message = "Price format is invalid")
    private BigDecimal price;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    /**
     * Calculates total amount needed for this buy order.
     * @return price * quantity
     */
    public BigDecimal getTotalAmount() {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Creates a description if none provided.
     * @return formatted description for the buy order
     */
    public String getOrCreateDescription() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return String.format("Buy order: %d %s @ %s", quantity, symbol, price);
    }
}