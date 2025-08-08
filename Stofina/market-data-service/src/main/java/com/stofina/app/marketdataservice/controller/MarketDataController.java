package com.stofina.app.marketdataservice.controller;


import com.stofina.app.marketdataservice.constants.Constants;
import com.stofina.app.marketdataservice.entity.Stock;
import com.stofina.app.marketdataservice.exceptions.InvalidSymbolException;
import com.stofina.app.marketdataservice.exceptions.StockNotFoundException;
import com.stofina.app.marketdataservice.service.IMarketHoursService;
import com.stofina.app.marketdataservice.service.IPriceSimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// CHECKPOINT 2.9: MarketDataController - Price Endpoints (Developer 2 Contribution)
@RestController
@RequestMapping("/api")
public class MarketDataController {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataController.class);

    @Autowired
    private IPriceSimulationService priceSimulationService;

    @Autowired
    private IMarketHoursService marketHoursService;

    @Autowired
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    // StockRepository kaldırıldı - artık PriceSimulationService'ten veri çekiliyor

    @GetMapping("/symbols")
    public ResponseEntity<List<Map<String, Object>>> getAllSymbols() {
        logger.info("Request for all symbols");
        
        Map<String, Stock> allStocks = priceSimulationService.getAllStocks();
        List<Map<String, Object>> symbolList = new ArrayList<>();
        
        for (Stock stock : allStocks.values()) {
            Map<String, Object> stockInfo = new HashMap<>();
            stockInfo.put("symbol", stock.getSymbol());
            stockInfo.put("companyName", stock.getCompanyName());
            stockInfo.put("currentPrice", stock.getCurrentPrice());
            stockInfo.put("change", stock.getCurrentPrice().subtract(stock.getPreviousClose()));
            stockInfo.put("lastUpdated", stock.getLastUpdated());
            symbolList.add(stockInfo);
        }
        
        logger.info("Returning {} symbols", symbolList.size());
        return ResponseEntity.ok(symbolList);
    }

    @GetMapping("/symbols/{symbol}/price")
    public ResponseEntity<Map<String, Object>> getCurrentPrice(@PathVariable String symbol) {
        logger.info("Price request for symbol: {}", symbol);

        // Validate symbol format
        if (symbol == null || symbol.trim().isEmpty() || symbol.length() > 10) {
            throw new InvalidSymbolException(symbol, Constants.ErrorMessages.INVALID_SYMBOL_FORMAT);
        }

        symbol = symbol.toUpperCase();

        // Önce cache'den kontrol et
        String cacheKey = Constants.Cache.PRICE_KEY_PREFIX + symbol;
        BigDecimal cachedPrice = getCachedPrice(cacheKey);
        
        if (cachedPrice != null) {
            logger.debug("Price found in cache for {}: {}", symbol, cachedPrice);
            return ResponseEntity.ok(createPriceResponse(symbol, cachedPrice, "CACHED"));
        }

        // Cache'de yoksa memory'den al (database yok artık)
        Stock stock = priceSimulationService.getStockBySymbol(symbol);
        if (stock == null) {
            throw new StockNotFoundException(symbol, Constants.ErrorMessages.STOCK_NOT_FOUND);
        }

        BigDecimal memoryPrice = stock.getCurrentPrice();
        logger.debug("Returning memory price for {}: {}", symbol, memoryPrice);
        return ResponseEntity.ok(createPriceResponse(symbol, memoryPrice, "MEMORY"));
    }

    @PostMapping("/prices")
    public ResponseEntity<List<Map<String, Object>>> getMultiplePrices(@RequestBody List<String> symbols) {
        logger.info("Multiple prices request for {} symbols", symbols != null ? symbols.size() : 0);

        // Validate symbols list
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException(Constants.ErrorMessages.SYMBOLS_LIST_EMPTY);
        }

        if (symbols.size() > 10) {
            throw new IllegalArgumentException(Constants.ErrorMessages.MAX_SYMBOLS_EXCEEDED);
        }

        List<Map<String, Object>> priceList = new ArrayList<>();
        
        for (String symbol : symbols) {
            if (symbol != null && !symbol.trim().isEmpty()) {
                String upperSymbol = symbol.toUpperCase();
                BigDecimal price = getCurrentPriceInternal(upperSymbol);
                priceList.add(createPriceResponse(upperSymbol, price, "BULK"));
            }
        }

        return ResponseEntity.ok(priceList);
    }

    @GetMapping("/symbols/{symbol}/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats(@PathVariable String symbol) {
        logger.info("Daily stats request for symbol: {}", symbol);

        // Validate symbol
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new InvalidSymbolException(symbol, Constants.ErrorMessages.INVALID_SYMBOL);
        }

        symbol = symbol.toUpperCase();
        Map<String, Object> dailyStats = createDailyStatsResponse(symbol);
        return ResponseEntity.ok(dailyStats);
    }

    // Helper methods
    private BigDecimal getCachedPrice(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey).block();
            if (cached instanceof BigDecimal) {
                return (BigDecimal) cached;
            }
        } catch (Exception e) {
            logger.debug("Cache miss for key: {}", cacheKey);
        }
        return null;
    }

    private BigDecimal getCurrentPriceInternal(String symbol) {
        String cacheKey = Constants.Cache.PRICE_KEY_PREFIX + symbol;
        BigDecimal cachedPrice = getCachedPrice(cacheKey);
        
        if (cachedPrice != null) {
            return cachedPrice;
        }
        
        // Fallback to memory (database yok artık)
        Stock stock = priceSimulationService.getStockBySymbol(symbol);
        return stock != null ? stock.getCurrentPrice() : new BigDecimal("100.00");
    }

    // getMockPrice kaldırıldı - artık priceSimulationService.getStockBySymbol() kullanılıyor

    private Map<String, Object> createPriceResponse(String symbol, BigDecimal price, String source) {
        Map<String, Object> response = new HashMap<>();
        response.put("symbol", symbol);
        response.put("price", price);
        response.put("timestamp", LocalDateTime.now());
        response.put("source", source);
        response.put("marketOpen", marketHoursService.isMarketOpen());
        return response;
    }

    private Map<String, Object> createDailyStatsResponse(String symbol) {
        Stock stock = priceSimulationService.getStockBySymbol(symbol);
        if (stock == null) {
            // Symbol bulunamazsa default değerler dön
            Map<String, Object> stats = new HashMap<>();
            stats.put("symbol", symbol);
            stats.put("error", "Stock not found");
            return stats;
        }
        
        BigDecimal currentPrice = stock.getCurrentPrice();
        BigDecimal previousClose = stock.getPreviousClose();
        BigDecimal change = currentPrice.subtract(previousClose);
        BigDecimal changePercent = previousClose.compareTo(BigDecimal.ZERO) > 0 ? 
            change.divide(previousClose, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : 
            BigDecimal.ZERO;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("symbol", symbol);
        stats.put("currentPrice", currentPrice);
        stats.put("high", stock.getDailyHigh());
        stats.put("low", stock.getDailyLow());
        stats.put("volume", 1250000L); // Mock volume - gerçek projede hesaplanabilir
        stats.put("change", change);
        stats.put("changePercent", changePercent);
        stats.put("previousClose", previousClose);
        stats.put("timestamp", stock.getLastUpdated());
        return stats;
    }

    // createErrorResponse kaldırıldı - GlobalExceptionHandler kullanılıyor
}