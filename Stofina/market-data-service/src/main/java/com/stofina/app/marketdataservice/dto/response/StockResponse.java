package com.stofina.app.marketdataservice.dto.response;

import com.stofina. app.marketdataservice.enums.StockStatus;
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
    private String stockName;
    private String companyName;
    private String equityMarket;
    private String exchange;
    private String currency;
    private String isinCode;
    private BigDecimal currentPrice;
    private BigDecimal defaultPrice;
    private BigDecimal changeAmount;
    private BigDecimal changePercent;
    private StockStatus status;
    private LocalDateTime lastUpdated;
}
