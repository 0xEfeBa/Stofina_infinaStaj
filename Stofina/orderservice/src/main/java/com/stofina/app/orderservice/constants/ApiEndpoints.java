package com.stofina.app.orderservice.constants;

public final class ApiEndpoints {
    
    // CLEAN CODE: API endpoint constants
    private ApiEndpoints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // API Version
    public static final String API_VERSION_V1 = "v1";
    public static final String API_BASE = "/api/" + API_VERSION_V1;
    public static final String ORDER_BOOK_BASE = API_BASE + "/orderbook";
    
    // Path Variables
    public static final String SYMBOL_PATH_VAR = "/{symbol}";
    
    // Order Book endpoint paths
    public static final String GET_SYMBOLS = "/symbols";
    public static final String GET_ORDER_BOOK = SYMBOL_PATH_VAR;
    public static final String GET_BEST_PRICES = SYMBOL_PATH_VAR + "/best-prices";
    public static final String GET_ORDER_BOOK_STATS = SYMBOL_PATH_VAR + "/stats";
    public static final String CREATE_ORDER = "/orders";
    
    // Full endpoint paths (for documentation/reference)
    public static final String SYMBOLS_ENDPOINT = ORDER_BOOK_BASE + GET_SYMBOLS;
    public static final String ORDER_BOOK_ENDPOINT = ORDER_BOOK_BASE + GET_ORDER_BOOK;
    public static final String BEST_PRICES_ENDPOINT = ORDER_BOOK_BASE + GET_BEST_PRICES;
    public static final String STATS_ENDPOINT = ORDER_BOOK_BASE + GET_ORDER_BOOK_STATS;
    public static final String ORDERS_ENDPOINT = ORDER_BOOK_BASE + CREATE_ORDER;
}