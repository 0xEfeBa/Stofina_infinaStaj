package com.stofina.app.marketdataservice.service.impl;

import com.stofina.app.marketdataservice.common.ServiceResult;
import com.stofina.app.marketdataservice.entity.Stock;

import java.math.BigDecimal;
import java.util.List;

public interface IStockService {

    ServiceResult<List<Stock>> getAllStocks();

    ServiceResult<Stock> getStockBySymbol(String symbol);

    ServiceResult<Stock> updateStockPrice(String symbol, BigDecimal newPrice);

    ServiceResult<List<Stock>> resetAllPricesToDefault();

    ServiceResult<Boolean>  isValidSymbol(String symbol);

    void save(Stock stock);


}
