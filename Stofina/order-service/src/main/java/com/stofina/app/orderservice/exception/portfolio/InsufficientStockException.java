package com.stofina.app.orderservice.exception.portfolio;

/**
 * Exception thrown when account has insufficient stock for a sell order.
 * This is a business rule violation that should not be retried.
 */
public class InsufficientStockException extends PortfolioServiceException {

    private final Long accountId;
    private final String symbol;
    private final Integer requiredQuantity;
    private final Integer availableQuantity;

    public InsufficientStockException(String message, Long accountId, String symbol) {
        super(message, "INSUFFICIENT_STOCK");
        this.accountId = accountId;
        this.symbol = symbol;
        this.requiredQuantity = null;
        this.availableQuantity = null;
    }

    public InsufficientStockException(String message, Long accountId, String symbol,
            Integer requiredQuantity, Integer availableQuantity) {
        super(message, "INSUFFICIENT_STOCK");
        this.accountId = accountId;
        this.symbol = symbol;
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getSymbol() {
        return symbol;
    }

    public Integer getRequiredQuantity() {
        return requiredQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    @Override
    public String getUserFriendlyMessage() {
        if (requiredQuantity != null && availableQuantity != null) {
            return String.format(
                "Insufficient %s stock. Required: %d, Available: %d", 
                symbol, requiredQuantity, availableQuantity
            );
        }
        return String.format("Insufficient %s stock for this operation.", symbol);
    }

    /**
     * Gets the shortage quantity.
     * @return the quantity lacking, or null if quantities are not available
     */
    public Integer getShortageQuantity() {
        if (requiredQuantity != null && availableQuantity != null) {
            return requiredQuantity - availableQuantity;
        }
        return null;
    }

    /**
     * Checks if account has any stock of this symbol.
     * @return true if available quantity is greater than zero
     */
    public boolean hasAnyStock() {
        return availableQuantity != null && availableQuantity > 0;
    }

    /**
     * Gets the shortage percentage.
     * @return the percentage of shortage, or null if quantities are not available
     */
    public Double getShortagePercentage() {
        if (requiredQuantity != null && availableQuantity != null && requiredQuantity > 0) {
            Integer shortage = getShortageQuantity();
            return (shortage.doubleValue() / requiredQuantity.doubleValue()) * 100.0;
        }
        return null;
    }
}