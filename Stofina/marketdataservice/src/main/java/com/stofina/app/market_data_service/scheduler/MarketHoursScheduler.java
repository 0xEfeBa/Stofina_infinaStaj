package com.stofina.app.market_data_service.scheduler;

import com.stofina.app.market_data_service.constants.Constants;
import com.stofina.app.market_data_service.service.IMarketHoursService;
import com.stofina.app.market_data_service.service.IPriceSimulationService;
import com.stofina.app.market_data_service.service.IWebSocketBroadcastService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// CHECKPOINT 2.8: Market Hours Scheduler
@Component
public class MarketHoursScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MarketHoursScheduler.class);

    @Autowired
    private IWebSocketBroadcastService broadcastService;

    @Autowired
    private IMarketHoursService marketHoursService;

    @Autowired
    private IPriceSimulationService priceSimulationService;

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

    // FIYAT GÜNCELLEMESİ: Sadece market saatleri içinde (09:30-18:00 Pazartesi-Cuma)
    @Scheduled(cron = "*/20 * 9-17 * * MON-FRI", zone = "Europe/Istanbul")
    public void updatePricesEvery20Seconds() {
        if (marketHoursService.isMarketOpen()) {
            logger.debug("Piyasa saatlerinde fiyat güncellemesi başladı");
            
            try {
                // 1. Algoritma ile fiyatları güncelle (Brownian Motion)
                priceSimulationService.simulateAllPrices();
                
                // 2. Güncellenmiş fiyatları WebSocket ile broadcast et
                priceSimulationService.broadcastCurrentPrices();
                
                logger.debug("Fiyat güncellemesi ve broadcast tamamlandı");
            } catch (Exception e) {
                logger.error("Fiyat güncelleme hatası: {}", e.getMessage(), e);
            }
        } else {
            logger.trace("Market kapalı - fiyat güncellemesi yapılmıyor");
        }
    }
    
    // Manuel test için market durumu kontrol (daha az sıklıkta)
    @Scheduled(fixedRate = 60000) // 1 dakikada bir
    public void logMarketStatus() {
        String status = marketHoursService.getMarketStatus();
        logger.debug("Market durumu: {}", status);
    }
}