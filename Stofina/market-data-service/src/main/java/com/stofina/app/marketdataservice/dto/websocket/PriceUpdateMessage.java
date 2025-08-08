package com.stofina.app.marketdataservice.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PriceUpdateMessage {
    private String symbol;
    private BigDecimal price;
    private BigDecimal changeAmount;
    private BigDecimal changePercent;
    private LocalDateTime timestamp;
    private String type = "PRICE_UPDATE";
    
    // Convenience constructor for backward compatibility
    public PriceUpdateMessage(String symbol, double price, double changeAmount, double changePercent, long timestamp) {
        this.symbol = symbol;
        this.price = BigDecimal.valueOf(price);
        this.changeAmount = BigDecimal.valueOf(changeAmount);
        this.changePercent = BigDecimal.valueOf(changePercent);
        this.timestamp = LocalDateTime.now();
        this.type = "PRICE_UPDATE";
    }
}