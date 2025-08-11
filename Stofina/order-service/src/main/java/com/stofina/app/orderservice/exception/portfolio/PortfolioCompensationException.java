package com.stofina.app.orderservice.exception.portfolio;

import com.stofina.app.orderservice.dto.portfolio.CompensationType;

/**
 * Exception thrown when Portfolio Service compensation operations fail.
 * This indicates a critical system error that requires manual intervention.
 */
public class PortfolioCompensationException extends PortfolioServiceException {

    private final Long orderId;
    private final Long tradeId;
    private final CompensationType compensationType;

    public PortfolioCompensationException(String message, Long orderId, CompensationType compensationType) {
        super(message, "PORTFOLIO_COMPENSATION_FAILED");
        this.orderId = orderId;
        this.tradeId = null;
        this.compensationType = compensationType;
    }

    public PortfolioCompensationException(String message, Long orderId, Long tradeId, 
            CompensationType compensationType, Throwable cause) {
        super(message, "PORTFOLIO_COMPENSATION_FAILED", cause);
        this.orderId = orderId;
        this.tradeId = tradeId;
        this.compensationType = compensationType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public CompensationType getCompensationType() {
        return compensationType;
    }

    @Override
    public String getUserFriendlyMessage() {
        return "System error occurred. Technical support has been notified and will resolve this issue.";
    }

    /**
     * Gets a detailed error description for technical logs.
     * @return detailed technical description
     */
    public String getTechnicalDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Portfolio compensation failed: ").append(compensationType.getDisplayName());
        desc.append(" for order ").append(orderId);
        
        if (tradeId != null) {
            desc.append(" (trade ").append(tradeId).append(")");
        }
        
        if (getCause() != null) {
            desc.append(" - Root cause: ").append(getCause().getMessage());
        }
        
        return desc.toString();
    }

    /**
     * Checks if this compensation failure affects balance.
     * @return true if balance is affected
     */
    public boolean affectsBalance() {
        return compensationType != null && compensationType.affectsBalance();
    }

    /**
     * Checks if this compensation failure affects stock positions.
     * @return true if stock positions are affected
     */
    public boolean affectsStockPosition() {
        return compensationType != null && compensationType.affectsStockPosition();
    }

    /**
     * Checks if this is a critical failure requiring immediate attention.
     * @return true if this is critical
     */
    public boolean isCritical() {
        return compensationType != null && compensationType.isCritical();
    }
}