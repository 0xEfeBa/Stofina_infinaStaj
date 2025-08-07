package com.stofina.app.market_data_service.service;

import com.stofina.app.market_data_service.dto.response.PriceResponse;

import java.util.List;

public interface IPriceSimulationService {

    
    void startPriceSimulation(String symbol);
    
    void stopPriceSimulation(String symbol);
  
    void stopAllSimulations();
  
    PriceResponse getSimulatedPrice(String symbol);
    
  
    List<PriceResponse> getAllSimulatedPrices();
    
  
    boolean isSimulationActive(String symbol);
    
   
    com.stofina.app.market_data_service.entity.Stock getStockBySymbol(String symbol);
    
    
    java.util.Map<String, com.stofina.app.market_data_service.entity.Stock> getAllStocks();
    
    /**
     * Fiyatları default değerlere sıfırlar
     */
    void resetPricesToDefault();
    
    /**
     * Güncel fiyatları WebSocket ile yayınlar
     */
    void broadcastCurrentPrices();
    
    /**
     * Tüm fiyatları simüle eder
     */
    void simulateAllPrices();
}
