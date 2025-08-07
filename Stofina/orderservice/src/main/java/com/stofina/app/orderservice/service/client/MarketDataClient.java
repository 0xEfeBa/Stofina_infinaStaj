package com.stofina.app.orderservice.service.client;

import com.stofina.app.orderservice.dto.response.external.PriceResponse;

import java.math.BigDecimal;
import java.util.List;

public interface MarketDataClient {

    BigDecimal getCurrentPrice(String symbol);

    PriceResponse getPriceWithDetails(String symbol);

    List<String> getSymbolList();

    boolean isMarketOpen();

    boolean validatePriceInRange(String symbol, BigDecimal price);

    boolean healthCheck();
}
