package com.stofina.app.orderservice.enums;

public enum MatchingStrategy {
    
    // CHECKPOINT 4.2 - User Order Matching Strategies
    
    FULL_FILL("User order will be completely filled", 30),
    PARTIAL_FILL("User order will be partially filled", 40), 
    NO_FILL("User order will not be filled", 30);
    
    private final String description;
    private final int probabilityPercent;
    
    MatchingStrategy(String description, int probabilityPercent) {
        this.description = description;
        this.probabilityPercent = probabilityPercent;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getProbabilityPercent() {
        return probabilityPercent;
    }
    
    public boolean isFullFill() {
        return this == FULL_FILL;
    }
    
    public boolean isPartialFill() {
        return this == PARTIAL_FILL;
    }
    
    public boolean isNoFill() {
        return this == NO_FILL;
    }
}