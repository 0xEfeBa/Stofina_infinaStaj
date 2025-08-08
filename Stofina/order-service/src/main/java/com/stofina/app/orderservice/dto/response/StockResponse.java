package com.stofina.app.orderservice.dto.response;

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
public class StockResponse {
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal defaultPrice;
    private BigDecimal changeAmount;
    private BigDecimal changePercent;
    private LocalDateTime lastUpdated;
}
