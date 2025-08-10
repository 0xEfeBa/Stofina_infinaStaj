package com.stofina.app.marketdataservice.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Market data update message matching frontend format
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarketDataUpdate {
    private String type; // PRICE_UPDATE, MARKET_STATUS, SYMBOL_LIST
    private String symbol;
    private Object data; // StockPrice, StockPrice[], or MarketStatus
    private String timestamp;
    
    public MarketDataUpdate(String type, String symbol, Object data) {
        this.type = type;
        this.symbol = symbol;
        this.data = data;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }
}