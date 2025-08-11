package com.stofina.app.orderservice.dto.portfolio;

import com.stofina.app.orderservice.enums.OrderType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for cancelling an order.
 * This is sent to Portfolio Service when an order needs to be cancelled
 * to reverse reservations or release reserved funds/stocks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancellationRequest {

    @NotNull(message = "Order ID cannot be null")
    @Positive(message = "Order ID must be positive")
    private Long orderId;

    @NotNull(message = "Account ID cannot be null")
    @Positive(message = "Account ID must be positive")
    private Long accountId;

    @NotNull(message = "Symbol cannot be null")
    private String symbol;

    @NotNull(message = "Order type cannot be null")
    private OrderType orderType;

    @NotNull(message = "Original quantity cannot be null")
    @Positive(message = "Original quantity must be positive")
    private Integer originalQuantity;

    @NotNull(message = "Filled quantity cannot be null")
    @PositiveOrZero(message = "Filled quantity must be zero or positive")
    private Integer filledQuantity;

    private String reason;

    /**
     * Gets the quantity that was not filled and needs to be unreserved.
     * @return unfilled quantity
     */
    public Integer getUnfilledQuantity() {
        if (originalQuantity != null && filledQuantity != null) {
            return originalQuantity - filledQuantity;
        }
        return null;
    }

    /**
     * Gets the fill percentage for this order.
     * @return percentage of order filled (0.0 to 1.0)
     */
    public Double getFillPercentage() {
        if (originalQuantity != null && originalQuantity > 0 && filledQuantity != null) {
            return filledQuantity.doubleValue() / originalQuantity.doubleValue();
        }
        return 0.0;
    }

    /**
     * Checks if this order was completely unfilled.
     * @return true if no quantity was filled
     */
    public boolean isCompletelyUnfilled() {
        return filledQuantity != null && filledQuantity == 0;
    }

    /**
     * Checks if this order was partially filled.
     * @return true if some quantity was filled but not all
     */
    public boolean isPartiallyFilled() {
        if (originalQuantity != null && filledQuantity != null) {
            return filledQuantity > 0 && filledQuantity < originalQuantity;
        }
        return false;
    }

    /**
     * Checks if this is a buy order type.
     * @return true if this is a buy order
     */
    public boolean isBuyOrder() {
        return orderType != null && 
               (orderType == OrderType.LIMIT_BUY || 
                orderType == OrderType.MARKET_BUY);
    }

    /**
     * Checks if this is a sell order type.
     * @return true if this is a sell order
     */
    public boolean isSellOrder() {
        return orderType != null && 
               (orderType == OrderType.LIMIT_SELL || 
                orderType == OrderType.MARKET_SELL ||
                orderType == OrderType.STOP_LOSS_SELL);
    }

    /**
     * Gets a description for this cancellation request.
     * @return formatted description string
     */
    public String getDescription() {
        if (reason != null && !reason.trim().isEmpty()) {
            return reason;
        }
        
        String status = isCompletelyUnfilled() ? "unfilled" : 
                       isPartiallyFilled() ? "partially filled" : "filled";
        
        return String.format("Cancellation of %s %s order for %d shares of %s (%s)", 
                status, orderType, originalQuantity, symbol, 
                isCompletelyUnfilled() ? "no execution" : 
                String.format("%d filled", filledQuantity));
    }

    /**
     * Validates that all required fields are present and valid.
     * @return true if request is valid
     */
    public boolean isValid() {
        return orderId != null && orderId > 0 &&
               accountId != null && accountId > 0 &&
               symbol != null && !symbol.trim().isEmpty() &&
               orderType != null &&
               originalQuantity != null && originalQuantity > 0 &&
               filledQuantity != null && filledQuantity >= 0 &&
               filledQuantity <= originalQuantity;
    }
}