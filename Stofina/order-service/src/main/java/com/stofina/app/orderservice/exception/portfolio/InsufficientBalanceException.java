package com.stofina.app.orderservice.exception.portfolio;

import java.math.BigDecimal;

/**
 * Exception thrown when account has insufficient balance for a buy order.
 * This is a business rule violation that should not be retried.
 */
public class InsufficientBalanceException extends PortfolioServiceException {

    private final Long accountId;
    private final BigDecimal requiredAmount;
    private final BigDecimal availableAmount;

    public InsufficientBalanceException(String message, Long accountId) {
        super(message, "INSUFFICIENT_BALANCE");
        this.accountId = accountId;
        this.requiredAmount = null;
        this.availableAmount = null;
    }

    public InsufficientBalanceException(String message, Long accountId, 
            BigDecimal requiredAmount, BigDecimal availableAmount) {
        super(message, "INSUFFICIENT_BALANCE");
        this.accountId = accountId;
        this.requiredAmount = requiredAmount;
        this.availableAmount = availableAmount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public BigDecimal getRequiredAmount() {
        return requiredAmount;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    @Override
    public String getUserFriendlyMessage() {
        if (requiredAmount != null && availableAmount != null) {
            return String.format(
                "Insufficient balance. Required: %s, Available: %s", 
                requiredAmount, availableAmount
            );
        }
        return "Insufficient balance for this operation.";
    }

    /**
     * Gets the shortage amount.
     * @return the amount lacking, or null if amounts are not available
     */
    public BigDecimal getShortageAmount() {
        if (requiredAmount != null && availableAmount != null) {
            return requiredAmount.subtract(availableAmount);
        }
        return null;
    }

    /**
     * Gets the shortage percentage.
     * @return the percentage of shortage, or null if amounts are not available
     */
    public BigDecimal getShortagePercentage() {
        if (requiredAmount != null && availableAmount != null && requiredAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal shortage = getShortageAmount();
            return shortage.divide(requiredAmount, 4, java.math.RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100));
        }
        return null;
    }
}