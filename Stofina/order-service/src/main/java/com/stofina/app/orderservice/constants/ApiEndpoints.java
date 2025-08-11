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
    public static final String ORDERS_BASE = API_BASE + "/orders";
    public static final String MARKET_DATA_BASE = API_BASE + "/market-data";
    
    // Path Variables
    public static final String SYMBOL_PATH_VAR = "/{symbol}";
    public static final String ORDER_ID_PATH_VAR = "/{orderId}";
    public static final String ACCOUNT_ID_PATH = "/account/{accountId}";
    public static final String SYMBOL_SEGMENT = "/symbol";
    
    // Order Book endpoint paths
    public static final String GET_SYMBOLS = "/symbols";
    public static final String GET_ORDER_BOOK = SYMBOL_PATH_VAR;
    public static final String GET_ORDER_BOOK_SNAPSHOT = SYMBOL_PATH_VAR + "/snapshot";
    public static final String GET_ORDER_BOOK_DEPTH = SYMBOL_PATH_VAR + "/depth";
    public static final String GET_BEST_PRICES = SYMBOL_PATH_VAR + "/best-prices";
    public static final String GET_ORDER_BOOK_STATS = SYMBOL_PATH_VAR + "/stats";
    public static final String CREATE_ORDER = "/orders";

    // Orders endpoint paths
    public static final String ORDER_BY_ID = ORDER_ID_PATH_VAR; // "/{orderId}"
    public static final String ORDERS_BY_ACCOUNT = ACCOUNT_ID_PATH; // "/account/{accountId}"
    public static final String ORDERS_BY_SYMBOL = SYMBOL_SEGMENT + SYMBOL_PATH_VAR; // "/symbol/{symbol}"
    public static final String ORDERS_VALIDATE = "/validate";

    // Market data endpoint paths
    public static final String MARKET_DATA_GET_SYMBOL = SYMBOL_PATH_VAR; // "/{symbol}"
    
    // Full endpoint paths (for documentation/reference)
    public static final String SYMBOLS_ENDPOINT = ORDER_BOOK_BASE + GET_SYMBOLS;
    public static final String ORDER_BOOK_ENDPOINT = ORDER_BOOK_BASE + GET_ORDER_BOOK;
    public static final String SNAPSHOT_ENDPOINT = ORDER_BOOK_BASE + GET_ORDER_BOOK_SNAPSHOT;
    public static final String DEPTH_ENDPOINT = ORDER_BOOK_BASE + GET_ORDER_BOOK_DEPTH;
    public static final String BEST_PRICES_ENDPOINT = ORDER_BOOK_BASE + GET_BEST_PRICES;
    public static final String STATS_ENDPOINT = ORDER_BOOK_BASE + GET_ORDER_BOOK_STATS;
    public static final String ORDERS_ENDPOINT = ORDER_BOOK_BASE + CREATE_ORDER;
    public static final String ORDERS_BASE_ENDPOINT = ORDERS_BASE;
    public static final String MARKET_DATA_BASE_ENDPOINT = MARKET_DATA_BASE;
}