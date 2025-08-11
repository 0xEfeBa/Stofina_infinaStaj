package com.stofina.app.orderservice.constants;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public final class MockDataConstants {
    
    // CLEAN CODE: Mock data constants - TODO: ENTEGRASYON SIRASINDA KALDIRILACAK
    private MockDataConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // BIST symbols for demo - Synced with Market Data Service
    public static final Set<String> BIST_SYMBOLS = Set.of(
        "AKBNK", "CCOLA", "DOAS", "MGROS", "FROTO",
        "TCELL", "THYAO", "YEOTK", "BRSAN", "TUPRS"
    );
    
    // Mock current prices - Synced with Market Data Service
    public static final Map<String, BigDecimal> INITIAL_PRICES = Map.of(
        "AKBNK", new BigDecimal("67.15"),
        "CCOLA", new BigDecimal("49.92"),
        "DOAS", new BigDecimal("183.10"),
        "MGROS", new BigDecimal("531.00"),
        "FROTO", new BigDecimal("92.95"),
        "TCELL", new BigDecimal("92.55"),
        "THYAO", new BigDecimal("290.25"),
        "YEOTK", new BigDecimal("42.08"),
        "BRSAN", new BigDecimal("354.00"),
        "TUPRS", new BigDecimal("164.50")
    );
    
    // Symbol volatility percentages
    public static final Map<String, Double> SYMBOL_VOLATILITIES = Map.of(
        "AKBNK", 0.3,   // Banks: Low volatility
        "CCOLA", 0.4,   // Consumer goods: Low-medium
        "DOAS", 0.6,    // Automotive: Medium
        "MGROS", 0.5,   // Retail: Medium
        "FROTO", 0.6,   // Automotive: Medium
        "TCELL", 0.4,   // Telecom: Low-medium
        "THYAO", 0.7,   // Airlines: High
        "YEOTK", 0.8,   // Small cap: High
        "BRSAN", 0.8,   // Industrial: High
        "TUPRS", 0.6    // Energy: Medium
    );
    
    // Default values
    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("50.00");
    public static final double DEFAULT_VOLATILITY = 0.5;
    
    // Response messages
    public static final String MOCK_DATA_TYPE = "MOCK_BIST_SYMBOLS";
}