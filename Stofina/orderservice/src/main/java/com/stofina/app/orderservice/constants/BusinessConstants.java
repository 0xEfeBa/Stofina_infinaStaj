package com.stofina.app.orderservice.constants;

import java.math.BigDecimal;

public final class BusinessConstants {
    
    // CLEAN CODE: Business logic constants
    private BusinessConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // Order validation
    public static final BigDecimal VALID_PRICE_RANGE_PERCENT = new BigDecimal("0.10"); // ±10%
    public static final BigDecimal WORSE_PRICE_OFFSET_PERCENT = new BigDecimal("0.02"); // 2% for NO_FILL
    
    // Strategy probabilities (must sum to 100)
    public static final int FULL_FILL_PROBABILITY = 30;
    public static final int PARTIAL_FILL_PROBABILITY = 40;
    public static final int NO_FILL_PROBABILITY = 30;
    
    // Partial fill quantity range
    public static final double MIN_PARTIAL_FILL_RATIO = 0.3; // 30%
    public static final double MAX_PARTIAL_FILL_RATIO = 0.8; // 80%
    
    // Display order book configuration
    public static final int DISPLAY_ORDERS_PER_SIDE = 25;
    public static final int TIER_1_ORDERS = 15; // ±0.5%
    public static final int TIER_2_ORDERS = 5;  // 0.5%-1%
    public static final int TIER_3_ORDERS = 5;  // 1%-3%
    
    // Price tier ranges
    public static final BigDecimal TIER_1_MAX_PERCENT = new BigDecimal("0.005"); // 0.5%
    public static final BigDecimal TIER_2_MAX_PERCENT = new BigDecimal("0.01");  // 1%
    public static final BigDecimal TIER_3_MAX_PERCENT = new BigDecimal("0.03");  // 3%
    
    // Quantity ranges
    public static final int TIER_1_MIN_QUANTITY = 500;
    public static final int TIER_1_MAX_QUANTITY = 2001;
    public static final int TIER_2_MIN_QUANTITY = 200;
    public static final int TIER_2_MAX_QUANTITY = 801;
    public static final int TIER_3_MIN_QUANTITY = 100;
    public static final int TIER_3_MAX_QUANTITY = 501;
    
    // Scheduled task configuration
    public static final long SCHEDULED_TASK_DELAY_MS = 10000L; // 10 seconds
    
    // Special account IDs
    public static final Long BOT_ACCOUNT_ID = 999999L;
    
    // Price precision
    public static final int PRICE_SCALE = 3; // 3 decimal places
    public static final int QUANTITY_SCALE = 0; // Whole numbers
}