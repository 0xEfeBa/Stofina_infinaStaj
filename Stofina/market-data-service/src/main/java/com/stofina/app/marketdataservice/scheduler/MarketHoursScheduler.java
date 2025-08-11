package com.stofina.app.marketdataservice.scheduler;

import com.stofina.app.marketdataservice.dto.response.StockResponse;
import com.stofina.app.marketdataservice.entity.Stock;
import com.stofina.app.marketdataservice.kafka.MarketDataProducer;
import com.stofina.app.marketdataservice.service.MarketHoursService;
import com.stofina.app.marketdataservice.service.PriceSimulationService;
import com.stofina.app.marketdataservice.service.WebSocketBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

// CHECKPOINT 2.8: Market Hours Scheduler
@Component
public class MarketHoursScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MarketHoursScheduler.class);

    @Autowired
    private WebSocketBroadcastService broadcastService;

    @Autowired
    private MarketHoursService marketHoursService;

    @Autowired
    private PriceSimulationService priceSimulationService;

    @Autowired
    private MarketDataProducer marketDataProducer;

    @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Europe/Istanbul")
    public void preMarketOpen() {
        logger.info("Pre-market: 30 dakika sonra market açılacak");
        
        try {
            LocalDateTime nextOpenTime = marketHoursService.getNextOpenTime();
            broadcastService.broadcastMarketStatus("PRE_MARKET", nextOpenTime, null);
            logger.info("Pre-market bildirimi gönderildi");
        } catch (Exception e) {
            logger.error("Pre-market bildirimi hatası: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 30 9 * * MON-FRI", zone = "Europe/Istanbul")
    public void marketOpen() {
        logger.info("Market AÇILDI - Trading başladı");
        
        try {
            LocalDateTime nextCloseTime = marketHoursService.getNextCloseTime();
            broadcastService.broadcastMarketStatus("OPEN", null, nextCloseTime);
            logger.info("Market açılış bildirimi gönderildi");
            
            // Market açılışında fiyatları default değerlere sıfırla (100 TL gibi)
            priceSimulationService.resetPricesToDefault();
            logger.info("Market açılışında fiyatlar default değerlere sıfırlandı");
            
            // Açılış fiyatlarını broadcast et
            priceSimulationService.broadcastCurrentPrices();
            
        } catch (Exception e) {
            logger.error("Market açılış bildirimi hatası: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 55 17 * * MON-FRI", zone = "Europe/Istanbul")
    public void marketCloseWarning() {
        logger.info("Market 5 dakika sonra kapanacak - son çağrı");
        
        try {
            LocalDateTime nextCloseTime = marketHoursService.getNextCloseTime();
            broadcastService.broadcastMarketStatus("CLOSING_SOON", null, nextCloseTime);
            logger.info("Market kapanış uyarısı gönderildi");
        } catch (Exception e) {
            logger.error("Market kapanış uyarısı hatası: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 18 * * MON-FRI", zone = "Europe/Istanbul")
    public void marketClose() {
        logger.info("Market KAPANDI - Trading sona erdi");
        
        try {
            LocalDateTime nextOpenTime = marketHoursService.getNextOpenTime();
            broadcastService.broadcastMarketStatus("CLOSED", nextOpenTime, null);
            logger.info("Market kapanış bildirimi gönderildi");
            
            // Market kapanışında fiyatları default değerlere sıfırla (105→100 TL)
            priceSimulationService.resetPricesToDefault();
            logger.info("Market kapanışında fiyatlar default değerlere sıfırlandı");
            
            // Kapanış fiyatlarını broadcast et
            priceSimulationService.broadcastCurrentPrices();
            
            // End of day summary broadcast
            broadcastEndOfDaySummary();
            
        } catch (Exception e) {
            logger.error("Market kapanış bildirimi hatası: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 19 * * MON-FRI", zone = "Europe/Istanbul")
    public void postMarketClose() {
        logger.info("Post-market: Market kapalı, sonraki açılış yarın");
        
        try {
            LocalDateTime nextOpenTime = marketHoursService.getNextOpenTime();
            broadcastService.broadcastMarketStatus("POST_MARKET", nextOpenTime, null);
            logger.info("Post-market bildirimi gönderildi");
        } catch (Exception e) {
            logger.error("Post-market bildirimi hatası: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 12 * * SAT", zone = "Europe/Istanbul")
    public void weekendStatus() {
        logger.info("Hafta sonu: Market kapalı");
        
        try {
            LocalDateTime nextMondayOpen = marketHoursService.getNextOpenTime();
            broadcastService.broadcastMarketStatus("WEEKEND", nextMondayOpen, null);
            logger.info("Hafta sonu bildirimi gönderildi");
        } catch (Exception e) {
            logger.error("Hafta sonu bildirimi hatası: {}", e.getMessage(), e);
        }
    }

    private void broadcastEndOfDaySummary() {
        logger.info("Günlük özet hazırlanıyor...");
        
        try {
            // Mock günlük özet verileri (StockService inject edilince gerçek data kullanılacak)
            broadcastService.broadcastMarketStatus("END_OF_DAY_SUMMARY", null, null);
            logger.info("Günlük özet broadcast edildi");
        } catch (Exception e) {
            logger.error("Günlük özet broadcast hatası: {}", e.getMessage(), e);
        }
    }

    // FIYAT GÜNCELLEMESİ: TEST MODU - Her 20 saniyede çalışır (Market saati kontrolü KAPALI)
    @Scheduled(cron = "*/5 * * * * *", zone = "Europe/Istanbul")
    public void updatePricesEvery20Seconds() {
        logger.debug("Fiyat güncellemesi başladı (Market saati kontrolü inaktif)");
        
        try {
            // 1. Algoritma ile fiyatları güncelle (Brownian Motion)
            priceSimulationService.simulateAllPrices();
            
            // 2. Güncellenmiş fiyatları WebSocket ile broadcast et
            priceSimulationService.broadcastCurrentPrices();
            
            // 3. KAFKA: Güncellenmiş fiyatları Order Service'e gönder
            sendUpdatedPricesToKafka();
            
            logger.info("Fiyat güncellemesi, WebSocket broadcast ve Kafka publish tamamlandı");
        } catch (Exception e) {
            logger.error("Fiyat güncelleme hatası: {}", e.getMessage(), e);
        }
    }
    
    // Manuel test için market durumu kontrol (daha az sıklıkta)
    @Scheduled(fixedRate = 60000) // 1 dakikada bir
    public void logMarketStatus() {
        String status = marketHoursService.getMarketStatus();
        logger.debug("Market durumu: {}", status);
    }

    private void sendUpdatedPricesToKafka() {
        try {
            var allStocks = priceSimulationService.getAllStocks();
            logger.debug("Kafka'ya {} adet hisse fiyatı gönderiliyor", allStocks.size());
            
            for (Stock stock : allStocks.values()) {
                StockResponse stockResponse = convertToStockResponse(stock);
                marketDataProducer.sendStockUpdate(stockResponse);
            }
            
            logger.info("Kafka'ya {} adet hisse fiyat güncellemesi gönderildi", allStocks.size());
        } catch (Exception e) {
            logger.error("Kafka fiyat gönderme hatası: {}", e.getMessage(), e);
        }
    }

    private StockResponse convertToStockResponse(Stock stock) {
        BigDecimal changeAmount = stock.getCurrentPrice().subtract(stock.getPreviousClose());
        BigDecimal changePercent = BigDecimal.ZERO;
        
        if (stock.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {
            changePercent = changeAmount.divide(stock.getPreviousClose(), 4, RoundingMode.HALF_UP)
                                      .multiply(new BigDecimal("100"))
                                      .setScale(2, RoundingMode.HALF_UP);
        }
        
        return new StockResponse(
                stock.getSymbol(),
                stock.getStockName(),
                stock.getCompanyName(),
                stock.getEquityMarket(),
                stock.getExchange(),
                stock.getCurrency(),
                stock.getIsinCode(),
                stock.getCurrentPrice(),
                stock.getDefaultPrice(),
                changeAmount,
                changePercent,
                stock.getStatus(),
                stock.getLastUpdated()
        );
    }
}