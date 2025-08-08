package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.constants.BusinessConstants;
import com.stofina.app.orderservice.constants.LogMessages;
import com.stofina.app.orderservice.constants.MockDataConstants;
import com.stofina.app.orderservice.dto.MatchingResult;
import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.Trade;
import com.stofina.app.orderservice.enums.MatchingStrategy;
import com.stofina.app.orderservice.enums.OrderSide;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.enums.OrderType;
import com.stofina.app.orderservice.service.UserOrderMatchingEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;

import java.util.Random;

@Service
@Slf4j
public class UserOrderMatchingEngineImpl implements UserOrderMatchingEngine {
    
    // CHECKPOINT 4.2 - User Order Matching Engine Implementation
    private final Random random = new Random();
    
    @Override
    public boolean isWithinValidRange(Order userOrder, BigDecimal currentPrice) {
        if (!isValidInput(userOrder, currentPrice)) {
            return false;
        }
        
        BigDecimal onePercent = currentPrice.multiply(BusinessConstants.VALID_PRICE_RANGE_PERCENT);
        BigDecimal minValidPrice = currentPrice.subtract(onePercent);
        BigDecimal maxValidPrice = currentPrice.add(onePercent);
        
        boolean withinRange = isWithinPriceRange(userOrder.getPrice(), minValidPrice, maxValidPrice);
        
        log.debug(LogMessages.RANGE_CHECK_DEBUG, 
                userOrder.getSymbol(), userOrder.getPrice(), minValidPrice, maxValidPrice, withinRange);
        
        return withinRange;
    }
    
    @Override
    public MatchingStrategy selectRandomStrategy() {
        int randomNum = random.nextInt(100);
        
        if (randomNum < BusinessConstants.FULL_FILL_PROBABILITY) {
            log.debug(LogMessages.STRATEGY_FULL_FILL, randomNum);
            return MatchingStrategy.FULL_FILL;
        } else if (randomNum < BusinessConstants.FULL_FILL_PROBABILITY + BusinessConstants.PARTIAL_FILL_PROBABILITY) {
            log.debug(LogMessages.STRATEGY_PARTIAL_FILL, randomNum);
            return MatchingStrategy.PARTIAL_FILL;
        } else {
            log.debug(LogMessages.STRATEGY_NO_FILL, randomNum);
            return MatchingStrategy.NO_FILL;
        }
    }
    
    @Override
    public MatchingResult processUserOrder(Order userOrder, BigDecimal currentPrice) {
        if (userOrder == null) {
            return MatchingResult.failed("User order is null");
        }
        
        log.info(LogMessages.ORDER_PROCESSING_START, 
                userOrder.getSide(), userOrder.getSymbol(), userOrder.getPrice(), userOrder.getQuantity());
        
        if (!isWithinValidRange(userOrder, currentPrice)) {
            return handleOutOfRangeOrder(userOrder, currentPrice);
        }
        
        MatchingStrategy strategy = selectRandomStrategy();
        log.info(LogMessages.STRATEGY_SELECTED, strategy, userOrder.getOrderId());
        
        Order counterBotOrder = generateCounterBotOrder(userOrder, strategy, currentPrice);
        if (counterBotOrder == null) {
            return MatchingResult.failed("Failed to generate counter-bot order");
        }
        
        MatchingResult result = executeMatching(userOrder, counterBotOrder, strategy);
        
        log.info(LogMessages.MATCHING_COMPLETED, 
                userOrder.getOrderId(), strategy, result.getFilledQuantity(), result.getTradeCount());
        
        return result;
    }
    
    @Override
    public Order generateCounterBotOrder(Order userOrder, MatchingStrategy strategy, BigDecimal currentPrice) {
        if (userOrder == null || strategy == null) {
            return null;
        }
        
        Order counterBot = new Order();
        counterBot.setOrderId(System.currentTimeMillis() + random.nextInt(1000));
        counterBot.setTenantId(userOrder.getTenantId());
        counterBot.setAccountId(999999L); // Special bot account ID
        counterBot.setSymbol(userOrder.getSymbol());
        counterBot.setOrderType(OrderType.LIMIT_BUY);
        counterBot.setSide(getOppositeSide(userOrder.getSide()));
        counterBot.setIsBot(true);
        counterBot.setStatus(OrderStatus.NEW);
        counterBot.setCreatedAt(LocalDateTime.now());
        counterBot.setFilledQuantity(BigDecimal.ZERO);
        
        switch (strategy) {
            case FULL_FILL:
                // User emriyle TAM eşleşecek bot emir
                counterBot.setPrice(userOrder.getPrice());
                counterBot.setQuantity(userOrder.getQuantity());
                // Note: remainingQuantity is calculated, not set directly
                break;
                
            case PARTIAL_FILL:
                BigDecimal partialQuantity = calculatePartialFillQuantity(userOrder.getQuantity());
                counterBot.setPrice(userOrder.getPrice());
                counterBot.setQuantity(partialQuantity);
                // Note: remainingQuantity is calculated, not set directly
                break;
                
            case NO_FILL:
                // User emrinden %2 kötü fiyat
                BigDecimal worsePrice = calculateWorsePrice(userOrder.getPrice(), userOrder.getSide(), currentPrice);
                counterBot.setPrice(worsePrice);
                counterBot.setQuantity(userOrder.getQuantity());
                // Note: remainingQuantity is calculated, not set directly
                break;
        }
        
        log.debug("Generated counter-bot order: {} {} @ {} × {} for strategy {}", 
                counterBot.getSide(), counterBot.getSymbol(), counterBot.getPrice(), 
                counterBot.getQuantity(), strategy);
        
        return counterBot;
    }
    
    @Override
    public MatchingResult executeMatching(Order userOrder, Order counterBotOrder, MatchingStrategy strategy) {
        if (userOrder == null || counterBotOrder == null) {
            return MatchingResult.failed("Invalid orders for matching");
        }
        
        // NO_FILL stratejisinde eşleşme yok
        if (strategy == MatchingStrategy.NO_FILL) {
            log.debug("NO_FILL strategy - no matching executed");
            // Note: remainingQuantity is calculated from quantity - filledQuantity
            return MatchingResult.successful(strategy, BigDecimal.ZERO, userOrder.getQuantity(), 
                                           Arrays.asList(), userOrder, counterBotOrder);
        }
        
        // Eşleştirme miktarını hesapla
        BigDecimal matchQuantity = userOrder.getQuantity().min(counterBotOrder.getQuantity());
        BigDecimal matchPrice = counterBotOrder.getPrice(); // Counter-bot'un fiyatı
        
        // Trade oluştur
        Trade trade = createTrade(userOrder, counterBotOrder, matchPrice, matchQuantity);
        
        // User order'ı güncelle
        userOrder.setFilledQuantity(userOrder.getFilledQuantity().add(matchQuantity));
        // Note: remainingQuantity is calculated automatically in getRemainingQuantity()
        
        if (userOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
            userOrder.setStatus(OrderStatus.FILLED);
        } else {
            userOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
        }
        
        userOrder.setUpdatedAt(LocalDateTime.now());
        
        // Average price hesapla (tek trade olduğu için match price)
        userOrder.setAveragePrice(matchPrice);
        
        // Counter-bot'u tamamlandı olarak işaretle
        counterBotOrder.setFilledQuantity(matchQuantity);
        // Note: remainingQuantity is calculated automatically in getRemainingQuantity()
        counterBotOrder.setStatus(OrderStatus.FILLED);
        counterBotOrder.setAveragePrice(matchPrice);
        counterBotOrder.setUpdatedAt(LocalDateTime.now());
        
        log.info("Trade executed: {} vs {} @ {} × {}", 
                userOrder.getOrderId(), counterBotOrder.getOrderId(), matchPrice, matchQuantity);
        
        return MatchingResult.successful(strategy, matchQuantity, userOrder.getRemainingQuantity(),
                                       Arrays.asList(trade), userOrder, counterBotOrder);
    }
    
    @Override
    public BigDecimal getCurrentPrice(String symbol) {
        return MockDataConstants.INITIAL_PRICES.getOrDefault(symbol, MockDataConstants.DEFAULT_PRICE);
    }
    
    @Override
    public BigDecimal calculateValidPriceRange(BigDecimal currentPrice, boolean isUpperBound) {
        BigDecimal onePercent = currentPrice.multiply(BusinessConstants.VALID_PRICE_RANGE_PERCENT);
        return isUpperBound ? currentPrice.add(onePercent) : currentPrice.subtract(onePercent);
    }
    
    private OrderSide getOppositeSide(OrderSide side) {
        return side == OrderSide.BUY ? OrderSide.SELL : OrderSide.BUY;
    }
    
    
    private Trade createTrade(Order buyOrder, Order sellOrder, BigDecimal price, BigDecimal quantity) {
        // Buy/Sell order'ları doğru sırada düzenle
        Order actualBuyOrder = buyOrder.getSide() == OrderSide.BUY ? buyOrder : sellOrder;
        Order actualSellOrder = buyOrder.getSide() == OrderSide.SELL ? buyOrder : sellOrder;
        
        Trade trade = new Trade();
        trade.setTradeId(System.currentTimeMillis() + random.nextInt(1000));
        trade.setBuyOrderId(actualBuyOrder.getOrderId());
        trade.setSellOrderId(actualSellOrder.getOrderId());
        trade.setSymbol(buyOrder.getSymbol());
        trade.setPrice(price);
        trade.setQuantity(quantity);
        trade.setExecutedAt(LocalDateTime.now());
        trade.setBuyAccountId(actualBuyOrder.getAccountId());
        trade.setSellAccountId(actualSellOrder.getAccountId());
        trade.setTenantId(buyOrder.getTenantId());
        
        return trade;
    }
    
    // Helper methods for clean code compliance
    private boolean isValidInput(Order userOrder, BigDecimal currentPrice) {
        return userOrder != null && userOrder.getPrice() != null && currentPrice != null;
    }
    
    private boolean isWithinPriceRange(BigDecimal price, BigDecimal minPrice, BigDecimal maxPrice) {
        return price.compareTo(minPrice) >= 0 && price.compareTo(maxPrice) <= 0;
    }
    
    private MatchingResult handleOutOfRangeOrder(Order userOrder, BigDecimal currentPrice) {
        String reason = String.format("Order price %s is outside ±10%% range of current price %s", 
                                    userOrder.getPrice(), currentPrice);
        log.warn(LogMessages.ORDER_REJECTED_OUT_OF_RANGE, reason);
        return MatchingResult.outOfRange(reason);
    }
    
    private BigDecimal calculateWorsePrice(BigDecimal userPrice, OrderSide userSide, BigDecimal currentPrice) {
        BigDecimal offset = currentPrice.multiply(BusinessConstants.WORSE_PRICE_OFFSET_PERCENT);
        
        if (userSide == OrderSide.BUY) {
            return userPrice.add(offset); // User alış, bot daha pahalı satış
        } else {
            return userPrice.subtract(offset); // User satış, bot daha ucuz alış
        }
    }
    
    private BigDecimal calculatePartialFillQuantity(BigDecimal originalQuantity) {
        double fillRatio = BusinessConstants.MIN_PARTIAL_FILL_RATIO + 
                          (random.nextDouble() * (BusinessConstants.MAX_PARTIAL_FILL_RATIO - BusinessConstants.MIN_PARTIAL_FILL_RATIO));
        
        BigDecimal partialQuantity = originalQuantity
            .multiply(BigDecimal.valueOf(fillRatio))
            .setScale(BusinessConstants.QUANTITY_SCALE, RoundingMode.HALF_UP);
        
        return partialQuantity.compareTo(BigDecimal.ONE) < 0 ? BigDecimal.ONE : partialQuantity;
    }
}