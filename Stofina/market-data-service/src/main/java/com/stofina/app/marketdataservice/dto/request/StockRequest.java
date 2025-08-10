package com.stofina.app.marketdataservice.dto.request;

import com.stofina.app.marketdataservice.enums.StockStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StockRequest {
    private String symbol;
    private String stockName;
    private String companyName;
    private String equityMarket;
    private String exchange;
    private String currency;
    private String isinCode;
    private StockStatus status;
    private BigDecimal defaultPrice;
    private BigDecimal currentPrice;
}
