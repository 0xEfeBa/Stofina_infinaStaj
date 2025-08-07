package com.stofina.app.orderservice.constants;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public final class MockDataConstants {
    
    // CLEAN CODE: Mock data constants - TODO: ENTEGRASYON SIRASINDA KALDIRILACAK
    private MockDataConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // BIST symbols for demo
    public static final Set<String> BIST_SYMBOLS = Set.of(
        "THYAO", "GARAN", "AKBNK", "ISCTR", "TUPRS",
        "ASELS", "SISE", "BIMAS", "KCHOL", "TCELL"
    );
    
    // Mock current prices
    public static final Map<String, BigDecimal> INITIAL_PRICES = Map.of(
        "THYAO", new BigDecimal("45.50"),
        "GARAN", new BigDecimal("28.75"),
        "AKBNK", new BigDecimal("42.30"),
        "ISCTR", new BigDecimal("15.80"),
        "TUPRS", new BigDecimal("38.90"),
        "ASELS", new BigDecimal("35.20"),
        "SISE", new BigDecimal("22.40"),
        "BIMAS", new BigDecimal("95.50"),
        "KCHOL", new BigDecimal("18.60"),
        "TCELL", new BigDecimal("52.80")
    );
    
    // Symbol volatility percentages
    public static final Map<String, Double> SYMBOL_VOLATILITIES = Map.of(
        "THYAO", 0.3,  // Banks: Low volatility
        "GARAN", 0.3,
        "AKBNK", 0.3,
        "TUPRS", 0.6,  // Tech/Industrial: Medium
        "ASELS", 0.6,
        "BIMAS", 0.8,  // Volatile stocks
        "TCELL", 0.8,
        "ISCTR", 0.5,  // Default medium
        "SISE", 0.5,
        "KCHOL", 0.5
    );
    
    // Default values
    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("50.00");
    public static final double DEFAULT_VOLATILITY = 0.5;
    
    // Response messages
    public static final String MOCK_DATA_TYPE = "MOCK_BIST_SYMBOLS";
}