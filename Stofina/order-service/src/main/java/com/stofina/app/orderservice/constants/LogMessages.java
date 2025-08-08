package com.stofina.app.orderservice.constants;

public final class LogMessages {
    
    // CLEAN CODE: Centralized log messages
    private LogMessages() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // Order processing messages
    public static final String ORDER_PROCESSING_START = "Processing user order: {} {} @ {} × {}";
    public static final String ORDER_PROCESSING_SUCCESS = "User order processed: strategy={}, filled={}, trades={}, status={}";
    public static final String ORDER_PROCESSING_ERROR = "Error processing user order: {}";
    public static final String ORDER_REJECTED_OUT_OF_RANGE = "Order rejected - out of range: {}";
    
    // Order book messages
    public static final String ORDER_BOOK_FETCH = "Fetching display order book for symbol: {}";
    public static final String ORDER_BOOK_RETURNED = "Display order book returned for {}: {} total orders";
    public static final String SYMBOL_NOT_FOUND = "Symbol not found in display order book: {}";
    
    // Display order book initialization
    public static final String DISPLAY_BOOK_INIT_START = "Initializing display order books for {} symbols";
    public static final String DISPLAY_BOOK_INIT_SYMBOL = "Display order book initialized for {}: {} BID, {} ASK orders";
    public static final String DISPLAY_BOOK_INIT_COMPLETE = "All display order books initialized: {} symbols";
    
    // Scheduled task messages
    public static final String SCHEDULED_MAINTENANCE_START = "🔄 Starting scheduled order book maintenance";
    public static final String SCHEDULED_MAINTENANCE_SUCCESS = "✅ Scheduled maintenance completed successfully";
    public static final String SCHEDULED_MAINTENANCE_ERROR = "❌ Error during scheduled maintenance: {}";
    public static final String SCHEDULED_TASK_ENABLED = "🟢 Scheduled order book maintenance enabled";
    public static final String SCHEDULED_TASK_DISABLED = "🔴 Scheduled order book maintenance disabled";
    
    // Price update messages
    public static final String PRICE_UPDATE_START = "📈 Updating market prices with volatility simulation";
    public static final String PRICE_UPDATE_SYMBOL = "Price updated for {}: {}";
    
    // User order matching messages
    public static final String USER_ORDERS_PROCESSING = "🔄 Processing active user orders for continuous matching";
    public static final String USER_ORDER_MATCHED = "🎯 User order matched in scheduled task: {} filled {} of {}";
    public static final String USER_ORDER_FILLED_REMOVED = "✅ User order completely filled and removed from display";
    public static final String USER_ORDER_PARTIAL_REMAINING = "📊 User order partially filled, {} remaining";
    
    // Strategy selection messages
    public static final String STRATEGY_SELECTED = "Selected strategy: {} for order {}";
    public static final String STRATEGY_FULL_FILL = "Selected strategy: FULL_FILL ({}%)";
    public static final String STRATEGY_PARTIAL_FILL = "Selected strategy: PARTIAL_FILL ({}%)";
    public static final String STRATEGY_NO_FILL = "Selected strategy: NO_FILL ({}%)";
    
    // Trade execution messages
    public static final String TRADE_EXECUTED = "Trade executed: {} vs {} @ {} × {}";
    public static final String MATCHING_COMPLETED = "Matching completed for order {}: strategy={}, filled={}, trades={}";
    
    // Range validation messages
    public static final String RANGE_CHECK_DEBUG = "Range check for {}: price={}, range=[{}-{}], valid={}";
    public static final String VALID_PRICE_RANGE = "✅ Valid price range: {} - {}";
    
    // Counter-bot generation messages
    public static final String COUNTER_BOT_GENERATED = "Generated counter-bot order: {} {} @ {} × {} for strategy {}";
}