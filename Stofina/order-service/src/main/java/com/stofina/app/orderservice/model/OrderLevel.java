package com.stofina.app.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderLevel {
    
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final int orderCount;
    
    public BigDecimal getTotalValue() {
        return price.multiply(quantity);
    }
}