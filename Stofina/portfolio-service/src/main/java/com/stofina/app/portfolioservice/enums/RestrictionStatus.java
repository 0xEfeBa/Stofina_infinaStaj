package com.stofina.app.portfolioservice.enums;

public enum RestrictionStatus {
    ACTIVE,     // T+2 restriction is active
    EXPIRED,    // Expired naturally (after settlement date)
    RELEASED,   // Manually released before expiry
    CANCELLED   // Sell order was cancelled, so restriction is void
}