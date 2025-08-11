package com.stofina.app.orderservice.dto.portfolio;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for confirming a completed trade.
 * This is sent to Portfolio Service when an order is fully executed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeConfirmationRequest {

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

    @NotNull(message = "Executed quantity cannot be null")
    @Positive(message = "Executed quantity must be positive")
    private Integer executedQuantity;

    @NotNull(message = "Executed price cannot be null")
    @Positive(message = "Executed price must be positive")
    private BigDecimal executedPrice;

    /**
     * Gets the total executed value (quantity * price).
     * @return total value of the executed trade
     */
    public BigDecimal getTotalExecutedValue() {
        if (executedQuantity != null && executedPrice != null) {
            return executedPrice.multiply(BigDecimal.valueOf(executedQuantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Gets a description for this trade confirmation.
     * @return formatted description string
     */
    public String getDescription() {
        return String.format("Trade confirmation for %d shares of %s at %s per share", 
                executedQuantity, symbol, executedPrice);
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
               executedQuantity != null && executedQuantity > 0 &&
               executedPrice != null && executedPrice.compareTo(BigDecimal.ZERO) > 0;
    }
}