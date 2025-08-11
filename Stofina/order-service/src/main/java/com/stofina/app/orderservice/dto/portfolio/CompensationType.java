package com.stofina.app.orderservice.dto.portfolio;

/**
 * Enum defining different types of compensation operations
 * that can be requested from Portfolio Service.
 */
public enum CompensationType {

    TRADE_ROLLBACK("Trade Rollback", "Rollback a completed trade due to system error"),
    
    RESERVATION_CANCELLATION("Reservation Cancellation", "Cancel balance/stock reservation"),
    
    PARTIAL_RESERVATION_RELEASE("Partial Reservation Release", "Release part of reserved amount"),
    
    BALANCE_CORRECTION("Balance Correction", "Correct account balance inconsistency"),
    
    POSITION_CORRECTION("Position Correction", "Correct stock position inconsistency"),
    
    SETTLEMENT_REVERSAL("Settlement Reversal", "Reverse settled transaction");

    private final String displayName;
    private final String description;

    CompensationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Checks if this compensation type affects balance.
     * @return true if this operation affects account balance
     */
    public boolean affectsBalance() {
        return this == TRADE_ROLLBACK || 
               this == RESERVATION_CANCELLATION || 
               this == BALANCE_CORRECTION ||
               this == SETTLEMENT_REVERSAL;
    }

    /**
     * Checks if this compensation type affects stock positions.
     * @return true if this operation affects stock positions
     */
    public boolean affectsStockPosition() {
        return this == TRADE_ROLLBACK || 
               this == POSITION_CORRECTION ||
               this == SETTLEMENT_REVERSAL;
    }

    /**
     * Checks if this is a critical operation that requires immediate attention.
     * @return true if this is a critical compensation
     */
    public boolean isCritical() {
        return this == BALANCE_CORRECTION || 
               this == POSITION_CORRECTION ||
               this == SETTLEMENT_REVERSAL;
    }
}