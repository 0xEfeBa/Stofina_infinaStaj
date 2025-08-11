package com.stofina.app.orderservice.dto.portfolio;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for Portfolio Service compensation operations.
 * Used when rollback/compensation is needed due to failed operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"orderId", "tradeId"})
public class CompensationRequest {

    @NotNull(message = "Order ID cannot be null")
    @Positive(message = "Order ID must be positive")
    private Long orderId;

    @Positive(message = "Trade ID must be positive if provided")
    private Long tradeId;

    @NotNull(message = "Compensation type cannot be null")
    private CompensationType compensationType;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0.01")
    @Digits(integer = 15, fraction = 4, message = "Amount format is invalid")
    private BigDecimal amount;

    @Positive(message = "Quantity must be positive if provided")
    private Integer quantity;

    @NotBlank(message = "Reason cannot be blank")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Size(max = 1000, message = "Additional details cannot exceed 1000 characters")
    private String additionalDetails;

    /**
     * Creates a trade rollback compensation request.
     * @param orderId the order ID
     * @param tradeId the trade ID to rollback
     * @param amount the amount to rollback
     * @param quantity the quantity to rollback
     * @param reason the reason for rollback
     * @return CompensationRequest for trade rollback
     */
    public static CompensationRequest forTradeRollback(Long orderId, Long tradeId, 
            BigDecimal amount, Integer quantity, String reason) {
        return CompensationRequest.builder()
                .orderId(orderId)
                .tradeId(tradeId)
                .compensationType(CompensationType.TRADE_ROLLBACK)
                .amount(amount)
                .quantity(quantity)
                .reason(reason)
                .build();
    }

    /**
     * Creates a reservation cancellation compensation request.
     * @param orderId the order ID
     * @param amount the amount to release
     * @param reason the reason for cancellation
     * @return CompensationRequest for reservation cancellation
     */
    public static CompensationRequest forReservationCancellation(Long orderId, 
            BigDecimal amount, String reason) {
        return CompensationRequest.builder()
                .orderId(orderId)
                .compensationType(CompensationType.RESERVATION_CANCELLATION)
                .amount(amount)
                .reason(reason)
                .build();
    }

    /**
     * Checks if this is a trade-related compensation.
     * @return true if this compensation involves a specific trade
     */
    public boolean isTradeRelated() {
        return tradeId != null;
    }

    /**
     * Gets a formatted summary of the compensation request.
     * @return human-readable summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(compensationType.getDisplayName());
        summary.append(" for order ").append(orderId);
        
        if (tradeId != null) {
            summary.append(" (trade ").append(tradeId).append(")");
        }
        
        if (amount != null) {
            summary.append(" - Amount: ").append(amount);
        }
        
        if (quantity != null) {
            summary.append(" - Quantity: ").append(quantity);
        }
        
        return summary.toString();
    }
}