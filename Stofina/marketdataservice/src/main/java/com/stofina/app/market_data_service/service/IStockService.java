package com.stofina.app.market_data_service.service;

import com.stofina.market_data_service.common.ServiceResult;
import com.stofina.market_data_service.entity.Stock;

import java.math.BigDecimal;
import java.util.List;

public interface IStockService {

    ServiceResult<List<Stock>> getAllStocks();

    ServiceResult<Stock> getStockBySymbol(String symbol);

    ServiceResult<Stock> updateStockPrice(String symbol, BigDecimal newPrice);

    ServiceResult<List<Stock>> resetAllPricesToDefault();

    ServiceResult<Boolean>  isValidSymbol(String symbol);

}