package com.stofina.app.orderservice.dto.response.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SymbolInfo {

    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal change;
    private BigDecimal changePercent;
    private Instant lastUpdated;

}