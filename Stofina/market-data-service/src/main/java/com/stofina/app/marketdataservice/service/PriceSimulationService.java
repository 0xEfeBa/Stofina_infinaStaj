package com.stofina.app.marketdataservice.service;

import com.stofina.app.marketdataservice.constant.Constants;
import com.stofina.app.marketdataservice.entity.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PostConstruct;

@Service
public class PriceSimulationService {

    private static final Logger logger = LoggerFactory.getLogger(PriceSimulationService.class);
    private final SecureRandom random = new SecureRandom(); 
    
    // In-memory stock storage (thread-safe)
    private final Map<String, Stock> stocksInMemory = new ConcurrentHashMap<>();

    @Autowired
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    // StockRepository kaldırıldı - artık database yok

    @Autowired
    private MarketHoursService marketHoursService;
    
    @Autowired
    private WebSocketBroadcastService webSocketBroadcastService;

    @PostConstruct
    public void initializeStocks() {
        logger.info("Initializing stocks in memory with default prices");
        
        // 10 hisse senedi ile default fiyatlarda başlat
        createStock(Constants.Stocks.AKBNK, Constants.Stocks.AKBNK_NAME, Constants.Stocks.AKBNK_DEFAULT_PRICE);
        createStock(Constants.Stocks.CCOLA, Constants.Stocks.CCOLA_NAME, Constants.Stocks.CCOLA_DEFAULT_PRICE);
        createStock(Constants.Stocks.DOAS, Constants.Stocks.DOAS_NAME, Constants.Stocks.DOAS_DEFAULT_PRICE);
        createStock(Constants.Stocks.MGROS, Constants.Stocks.MGROS_NAME, Constants.Stocks.MGROS_DEFAULT_PRICE);
        createStock(Constants.Stocks.FROTO, Constants.Stocks.FROTO_NAME, Constants.Stocks.FROTO_DEFAULT_PRICE);
        createStock(Constants.Stocks.TCELL, Constants.Stocks.TCELL_NAME, Constants.Stocks.TCELL_DEFAULT_PRICE);
        createStock(Constants.Stocks.THYAO, Constants.Stocks.THYAO_NAME, Constants.Stocks.THYAO_DEFAULT_PRICE);
        createStock(Constants.Stocks.YEOTK, Constants.Stocks.YEOTK_NAME, Constants.Stocks.YEOTK_DEFAULT_PRICE);
        createStock(Constants.Stocks.BRSAN, Constants.Stocks.BRSAN_NAME, Constants.Stocks.BRSAN_DEFAULT_PRICE);
        createStock(Constants.Stocks.TUPRS, Constants.Stocks.TUPRS_NAME, Constants.Stocks.TUPRS_DEFAULT_PRICE);
        
        logger.info("Initialized {} stocks in memory", stocksInMemory.size());
    }
    
    private void createStock(String symbol, String companyName, BigDecimal defaultPrice) {
        Stock stock = new Stock();
        stock.setSymbol(symbol);
        stock.setCompanyName(companyName);
        stock.setDefaultPrice(defaultPrice);
        stock.setCurrentPrice(defaultPrice);
        stock.setDailyHigh(defaultPrice);
        stock.setDailyLow(defaultPrice);
        stock.setPreviousClose(defaultPrice);
        stock.setLastUpdated(LocalDateTime.now());
        stock.setCreatedAt(LocalDateTime.now());
        stock.setUpdatedAt(LocalDateTime.now());
        
        stocksInMemory.put(symbol, stock);
        
        // Redis'e de kaydet
        String cacheKey = Constants.Cache.PRICE_KEY_PREFIX + symbol;
        try {
            redisTemplate.opsForValue()
                .set(cacheKey, defaultPrice, Duration.ofSeconds(Constants.Cache.PRICE_TTL_SECONDS))
                .subscribe();
        } catch (Exception e) {
            logger.warn("Redis cache failed during initialization for {}: {}", symbol, e.getMessage());
        }
    }

    public void updateAllStockPrices() {
        // Market saatleri kontrolü
        if (!marketHoursService.isMarketOpen()) {
            logger.debug("Market closed - price updates paused. Status: {}", marketHoursService.getMarketStatus());
            return;
        }
        
        logger.debug("Starting price update cycle for all stocks");
        updateStockPricesInMemory();
    }

    private void updateStockPricesInMemory() {
        for (Stock stock : stocksInMemory.values()) {
            BigDecimal oldPrice = stock.getCurrentPrice();
            BigDecimal newPrice = calculateNewPrice(oldPrice);
            
            if (isPriceWithinLimits(newPrice, stock.getDefaultPrice())) {
                // Update in memory only (no database)
                stock.setCurrentPrice(newPrice);
                stock.setLastUpdated(LocalDateTime.now());
                
                // Update daily high/low
                if (stock.getDailyHigh() == null || newPrice.compareTo(stock.getDailyHigh()) > 0) {
                    stock.setDailyHigh(newPrice);
                }
                if (stock.getDailyLow() == null || newPrice.compareTo(stock.getDailyLow()) < 0) {
                    stock.setDailyLow(newPrice);
                }
                
                // Cache price in Redis (TTL from Constants)
                String cacheKey = Constants.Cache.PRICE_KEY_PREFIX + stock.getSymbol();
                try {
                    redisTemplate.opsForValue()
                        .set(cacheKey, newPrice, Duration.ofSeconds(Constants.Cache.PRICE_TTL_SECONDS))
                        .subscribe();
                } catch (Exception e) {
                    logger.warn("Redis cache failed for {}: {}", stock.getSymbol(), e.getMessage());
                }
                
                logger.info("Price updated - {}: {} -> {} (High: {}, Low: {})", 
                    stock.getSymbol(), oldPrice, newPrice, stock.getDailyHigh(), stock.getDailyLow());
            }
        }
    }

    private void simulatePriceUpdatesOLD() {
        // Mock stocks - StockService inject edilince gerçek data kullanılacak
        String[] symbols = {Constants.Stocks.THYAO, Constants.Stocks.AKBNK, Constants.Stocks.CCOLA, Constants.Stocks.FROTO, Constants.Stocks.TUPRS};
        BigDecimal[] currentPrices = {
            Constants.Stocks.THYAO_DEFAULT_PRICE,
            Constants.Stocks.AKBNK_DEFAULT_PRICE, 
            Constants.Stocks.CCOLA_DEFAULT_PRICE,
            Constants.Stocks.FROTO_DEFAULT_PRICE,
            Constants.Stocks.TUPRS_DEFAULT_PRICE
        };
        BigDecimal[] defaultPrices = {
            Constants.Stocks.THYAO_DEFAULT_PRICE,
            Constants.Stocks.AKBNK_DEFAULT_PRICE,
            Constants.Stocks.CCOLA_DEFAULT_PRICE, 
            Constants.Stocks.FROTO_DEFAULT_PRICE,
            Constants.Stocks.TUPRS_DEFAULT_PRICE
        };

        for (int i = 0; i < symbols.length; i++) {
            BigDecimal newPrice = calculateNewPrice(currentPrices[i]);
            
            if (isPriceWithinLimits(newPrice, defaultPrices[i])) {
                // Cache price in Redis (TTL from Constants)
                String cacheKey = Constants.Cache.PRICE_KEY_PREFIX + symbols[i];
                try {
                    redisTemplate.opsForValue()
                        .set(cacheKey, newPrice, Duration.ofSeconds(Constants.Cache.PRICE_TTL_SECONDS))
                        .subscribe();
                } catch (Exception e) {
                    System.out.println("Redis not available, skipping cache: " + e.getMessage());
                }
                
                System.out.println(symbols[i] + ": " + currentPrices[i] + " -> " + newPrice);
                currentPrices[i] = newPrice; // Sonraki iterasyon için güncelle
            }
        }
    }

    public BigDecimal calculateNewPrice(BigDecimal currentPrice) {
        // -1 ile +1 arası rastgele faktör
        double randomFactor = (random.nextDouble() * 2.0) - 1.0;
        
        // Maksimum fiyat değişimi hesapla
        BigDecimal maxPriceChange = currentPrice.multiply(new BigDecimal(Constants.PriceSimulation.MAX_CHANGE_PERCENT));
        
        // Rastgele faktör ile çarp
        BigDecimal priceChange = maxPriceChange.multiply(new BigDecimal(randomFactor));
        
        // Yeni fiyatı hesapla
        BigDecimal newPrice = currentPrice.add(priceChange);
        
        // 2 ondalık basamağa yuvarla
        return newPrice.setScale(Constants.PriceSimulation.DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    public boolean isPriceWithinLimits(BigDecimal newPrice, BigDecimal defaultPrice) {
        BigDecimal maxPrice = defaultPrice.multiply(new BigDecimal(1 + Constants.PriceSimulation.DAILY_LIMIT_PERCENT));
        BigDecimal minPrice = defaultPrice.multiply(new BigDecimal(1 - Constants.PriceSimulation.DAILY_LIMIT_PERCENT));
        
        return newPrice.compareTo(minPrice) >= 0 && newPrice.compareTo(maxPrice) <= 0;
    }

    public void resetPricesToDefault() {
        logger.info("Resetting all stock prices to default values");
        
        for (Stock stock : stocksInMemory.values()) {
            BigDecimal defaultPrice = stock.getDefaultPrice();
            stock.setCurrentPrice(defaultPrice);
            stock.setDailyHigh(defaultPrice);
            stock.setDailyLow(defaultPrice);
            stock.setPreviousClose(defaultPrice);
            stock.setLastUpdated(LocalDateTime.now());
            stock.setUpdatedAt(LocalDateTime.now());
            
            // Redis'e de güncel fiyatı kaydet
            String cacheKey = Constants.Cache.PRICE_KEY_PREFIX + stock.getSymbol();
            try {
                redisTemplate.opsForValue()
                    .set(cacheKey, defaultPrice, Duration.ofSeconds(Constants.Cache.PRICE_TTL_SECONDS))
                    .subscribe();
            } catch (Exception e) {
                logger.warn("Redis cache failed during reset for {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
        
        logger.info("Reset completed for {} stocks", stocksInMemory.size());
    }
    
    // Public methods for Controller access
    public Stock getStockBySymbol(String symbol) {
        return stocksInMemory.get(symbol.toUpperCase());
    }
    
    public Map<String, Stock> getAllStocks() {
        return new HashMap<>(stocksInMemory); // Return copy to prevent external modification
    }
    
    /**
     * ALIAS for updateAllStockPrices() - MarketHoursScheduler compatibility
     */
    public void simulateAllPrices() {
        updateAllStockPrices();
    }
    
    /**
     * WebSocket broadcast method for MarketHoursScheduler
     * GERÇEK WebSocket broadcasting ile client'lara gönderir
     */
    public void broadcastCurrentPrices() {
        logger.debug("Broadcasting current prices for {} stocks", stocksInMemory.size());
        
        for (Stock stock : stocksInMemory.values()) {
            BigDecimal currentPrice = stock.getCurrentPrice();
            BigDecimal previousClose = stock.getPreviousClose();
            BigDecimal changeAmount = currentPrice.subtract(previousClose);
            
            // Calculate percentage change
            BigDecimal changePercent = BigDecimal.ZERO;
            if (previousClose.compareTo(BigDecimal.ZERO) > 0) {
                changePercent = changeAmount.divide(previousClose, 4, RoundingMode.HALF_UP)
                                          .multiply(new BigDecimal("100"))
                                          .setScale(2, RoundingMode.HALF_UP);
            }
            
            // GERÇEK WebSocket broadcast - tüm client'lara gönder
            webSocketBroadcastService.broadcastPriceUpdate(
                stock.getSymbol(), 
                currentPrice, 
                changeAmount, 
                changePercent
            );
            
            logger.debug("Broadcasted {} - Price: {}, Change: {}%", 
                stock.getSymbol(), currentPrice, changePercent);
        }
        
        logger.info("WebSocket price broadcast completed for {} stocks", stocksInMemory.size());
    }
}