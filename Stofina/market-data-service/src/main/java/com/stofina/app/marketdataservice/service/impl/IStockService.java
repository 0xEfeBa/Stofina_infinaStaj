package com.stofina.app.marketdataservice.service.impl;

import com.stofina.app.marketdataservice.common.ServiceResult;
import com.stofina.app.marketdataservice.entity.Stock;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for managing stock operations.
 * Provides methods for retrieving, updating, and managing stock information.
 */
public interface IStockService {

    /**
     * Retrieves all stocks in the system.
     *
     * @return ServiceResult containing a list of all stocks if successful
     */
    ServiceResult<List<Stock>> getAllStocks();

    /**
     * Retrieves a specific stock by its symbol.
     *
     * @param symbol the stock symbol to look up
     * @return ServiceResult containing the found stock if successful
     */
    ServiceResult<Stock> getStockBySymbol(String symbol);

    /**
     * Updates the price of a specific stock.
     *
     * @param symbol the stock symbol to update
     * @param newPrice the new price to set
     * @return ServiceResult containing the updated stock if successful
     */
    ServiceResult<Stock> updateStockPrice(String symbol, BigDecimal newPrice);

    /**
     * Resets all stock prices to their default values.
     *
     * @return ServiceResult containing a list of all stocks with reset prices
     */
    ServiceResult<List<Stock>> resetAllPricesToDefault();

    /**
     * Checks if a given stock symbol is valid.
     *
     * @param symbol the stock symbol to validate
     * @return ServiceResult containing true if the symbol is valid, false otherwise
     */
    ServiceResult<Boolean> isValidSymbol(String symbol);

    /**
     * Saves a stock entity to the repository.
     *
     * @param stock the stock entity to save
     */
    void save(Stock stock);


}
