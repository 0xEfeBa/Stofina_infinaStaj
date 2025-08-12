package com.stofina.app.marketdataservice.service.impl;

import com.stofina.app.marketdataservice.dto.response.PriceResponse;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Service interface for retrieving market statistics.
 * Provides methods for accessing price-related statistics for all stocks.
 * All methods in this interface are validated using Spring's validation framework.
 */
@Validated
public interface IStatisticsService {
    /**
     * Retrieves current prices for all stocks in the system.
     *
     * @return List of PriceResponse objects containing current price information
     */
    List<PriceResponse> getAllPrices();
}
