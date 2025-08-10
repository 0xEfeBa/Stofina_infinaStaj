package com.stofina.app.marketdataservice.mapper;


import com.stofina.app.marketdataservice.dto.request.StockRequest;
import com.stofina.app.marketdataservice.dto.response.StockResponse;
import com.stofina.app.marketdataservice.entity.Stock;

public class StockMapper {

    public static Stock toEntity(StockRequest request) {
        Stock stock = new Stock();
        stock.setSymbol(request.getSymbol());
        stock.setStockName(request.getStockName());
        stock.setCompanyName(request.getCompanyName());
        stock.setEquityMarket(request.getEquityMarket());
        stock.setExchange(request.getExchange());
        stock.setCurrency(request.getCurrency());
        stock.setIsinCode(request.getIsinCode());
        stock.setStatus(request.getStatus());
        stock.setDefaultPrice(request.getDefaultPrice() != null ? request.getDefaultPrice() :
                java.math.BigDecimal.ZERO);
        stock.setCurrentPrice(request.getCurrentPrice() != null ? request.getCurrentPrice() :
                java.math.BigDecimal.ZERO);
        return stock;
    }

    public static StockResponse toResponse(Stock stock) {
        StockResponse response = new StockResponse();
        response.setSymbol(stock.getSymbol());
        response.setStockName(stock.getStockName());
        response.setCompanyName(stock.getCompanyName());
        response.setEquityMarket(stock.getEquityMarket());
        response.setExchange(stock.getExchange());
        response.setCurrency(stock.getCurrency());
        response.setIsinCode(stock.getIsinCode());
        response.setStatus(stock.getStatus());
        response.setCurrentPrice(stock.getCurrentPrice());
        response.setDefaultPrice(stock.getDefaultPrice());
        response.setChangeAmount(
                stock.getPreviousClose() != null ? stock.getCurrentPrice().subtract(stock.getPreviousClose()) : null
        );
        response.setChangePercent(
                (stock.getPreviousClose() != null && stock.getPreviousClose().compareTo(java.math.BigDecimal.ZERO) > 0)
                        ? stock.getCurrentPrice()
                        .subtract(stock.getPreviousClose())
                        .divide(stock.getPreviousClose(), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(new java.math.BigDecimal("100"))
                        : null
        );
        response.setLastUpdated(stock.getLastUpdated());
        return response;
    }
}
