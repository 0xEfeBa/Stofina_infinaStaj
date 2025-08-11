package com.stofina.app.orderservice.dto.portfolio;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for confirming a partial trade execution.
 * This is sent to Portfolio Service when an order is partially filled.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartialTradeConfirmationRequest {

    @NotNull(message = "Trade ID cannot be null")
    @Positive(message = "Trade ID must be positive")
    private Long tradeId;

    @NotNull(message = "Order ID cannot be null")
    @Positive(message = "Order ID must be positive")
    private Long orderId;

    @NotNull(message = "Account ID cannot be null")
    @Positive(message = "Account ID must be positive")
    private Long accountId;

    @NotNull(message = "Symbol cannot be null")
    private String symbol;

    @NotNull(message = "Partial quantity cannot be null")
    @Positive(message = "Partial quantity must be positive")
    private Integer partialQuantity;

    @NotNull(message = "Remaining quantity cannot be null")
    @PositiveOrZero(message = "Remaining quantity must be zero or positive")
    private Integer remainingQuantity;

    @NotNull(message = "Executed price cannot be null")
    @Positive(message = "Executed price must be positive")
    private BigDecimal executedPrice;

    /**
     * Gets the total executed value for this partial trade (partialQuantity * price).
     * @return total value of the partial execution
     */
    public BigDecimal getPartialExecutedValue() {
        if (partialQuantity != null && executedPrice != null) {
            return executedPrice.multiply(BigDecimal.valueOf(partialQuantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Gets the original order quantity (partialQuantity + remainingQuantity).
     * @return original order quantity
     */
    public Integer getOriginalQuantity() {
        if (partialQuantity != null && remainingQuantity != null) {
            return partialQuantity + remainingQuantity;
        }
        return null;
    }

    /**
     * Gets the fill percentage for this order.
     * @return percentage of order filled (0.0 to 1.0)
     */
    public Double getFillPercentage() {
        Integer originalQty = getOriginalQuantity();
        if (originalQty != null && originalQty > 0) {
            return partialQuantity.doubleValue() / originalQty.doubleValue();
        }
        return null;
    }

    /**
     * Checks if this is the final partial fill (remaining quantity is 0).
     * @return true if this partial fill completes the order
     */
    public boolean isFinalPartialFill() {
        return remainingQuantity != null && remainingQuantity == 0;
    }

    /**
     * Gets a description for this partial trade confirmation.
     * @return formatted description string
     */
    public String getDescription() {
        return String.format("Partial trade confirmation for %d/%d shares of %s at %s per share", 
                partialQuantity, getOriginalQuantity(), symbol, executedPrice);
    }

    /**
     * Validates that all required fields are present and valid.
     * @return true if request is valid
     */
    public boolean isValid() {
        return tradeId != null && tradeId > 0 &&
               orderId != null && orderId > 0 &&
               accountId != null && accountId > 0 &&
               symbol != null && !symbol.trim().isEmpty() &&
               partialQuantity != null && partialQuantity > 0 &&
               remainingQuantity != null && remainingQuantity >= 0 &&
               executedPrice != null && executedPrice.compareTo(BigDecimal.ZERO) > 0;
    }
}