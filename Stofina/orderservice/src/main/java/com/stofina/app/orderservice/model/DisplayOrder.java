package com.stofina.app.orderservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.stofina.app.orderservice.enums.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisplayOrder {
    
    // CHECKPOINT 4.1 - Display-Only Bot Order Model
    private Long displayOrderId;
    private String symbol;
    private OrderSide side;
    private BigDecimal price;
    private BigDecimal quantity;
    private boolean isBot;
    private LocalDateTime createdAt;
    
    public DisplayOrder(String symbol, OrderSide side, BigDecimal price, BigDecimal quantity, boolean isBot) {
        this.displayOrderId = System.currentTimeMillis() + (long)(Math.random() * 1000);
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.isBot = isBot;
        this.createdAt = LocalDateTime.now();
    }
    
    public boolean isBuy() {
        return OrderSide.BUY.equals(side);
    }
    
    public boolean isSell() {
        return OrderSide.SELL.equals(side);
    }
    
    public String getFormattedPrice() {
        return price != null ? price.toString() : "0.00";
    }
    
    public String getFormattedQuantity() {
        return quantity != null ? quantity.toString() : "0";
    }
}