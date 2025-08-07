package com.stofina.app.orderservice.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KafkaPriceCache {

    private final Map<String, PriceCacheEntry> priceCache = new ConcurrentHashMap<>();
    
    public void updatePrice(String symbol, BigDecimal price) {
        priceCache.put(symbol, new PriceCacheEntry(price, LocalDateTime.now()));
    }
    
    public BigDecimal getCurrentPrice(String symbol) {
        PriceCacheEntry entry = priceCache.get(symbol);
        if (entry == null) {
            return null;
        }
        
        // 5 dakikadan eski fiyatları geçersiz say
        if (entry.getUpdatedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
            priceCache.remove(symbol);
            return null;
        }
        
        return entry.getPrice();
    }
    
    public LocalDateTime getLastUpdateTime(String symbol) {
        PriceCacheEntry entry = priceCache.get(symbol);
        return entry != null ? entry.getUpdatedAt() : null;
    }
    
    public boolean hasPrice(String symbol) {
        return getCurrentPrice(symbol) != null;
    }
    
    public void clearCache() {
        priceCache.clear();
    }
    
    public Map<String, BigDecimal> getAllPrices() {
        Map<String, BigDecimal> currentPrices = new ConcurrentHashMap<>();
        priceCache.forEach((symbol, entry) -> {
            BigDecimal price = getCurrentPrice(symbol);
            if (price != null) {
                currentPrices.put(symbol, price);
            }
        });
        return currentPrices;
    }
    
    private static class PriceCacheEntry {
        private final BigDecimal price;
        private final LocalDateTime updatedAt;
        
        public PriceCacheEntry(BigDecimal price, LocalDateTime updatedAt) {
            this.price = price;
            this.updatedAt = updatedAt;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }
    }
}