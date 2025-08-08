package com.stofina.app.marketdataservice.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Stock price data matching frontend StockPrice interface
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockPrice {
    private String symbol;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;
    private Long volume;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private String timestamp;
    private Long marketCap;
    
    // Convenience constructor for basic price data
    public StockPrice(String symbol, double price, double change, double changePercent) {
        this.symbol = symbol;
        this.price = BigDecimal.valueOf(price);
        this.change = BigDecimal.valueOf(change);
        this.changePercent = BigDecimal.valueOf(changePercent);
        this.volume = 1000000L; // Mock volume
        this.open = BigDecimal.valueOf(price * 0.98); // Mock open price
        this.high = BigDecimal.valueOf(price * 1.05); // Mock high price
        this.low = BigDecimal.valueOf(price * 0.95);  // Mock low price
        this.timestamp = java.time.LocalDateTime.now().toString();
        this.marketCap = 5000000000L; // Mock market cap
    }
}