package com.stofina.app.market_data_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockResponse {
    private String symbol;
    private String companyName;
    private double currentPrice;
    private double defaultPrice;
    private double changeAmount;
    private double changePercent;
    private LocalDateTime lastUpdated;
}
