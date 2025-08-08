package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.constants.BusinessConstants;
import com.stofina.app.orderservice.constants.LogMessages;
import com.stofina.app.orderservice.constants.MockDataConstants;
import com.stofina.app.orderservice.dto.MatchingResult;
import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.model.DisplayOrder;
import com.stofina.app.orderservice.service.DisplayOrderBookService;
import com.stofina.app.orderservice.service.ScheduledOrderBookService;
import com.stofina.app.orderservice.service.UserOrderMatchingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledOrderBookServiceImpl implements ScheduledOrderBookService {
    
    // CHECKPOINT 6.2 - Scheduled Order Book Service Implementation
    private final DisplayOrderBookService displayOrderBookService;
    private final UserOrderMatchingEngine userOrderMatchingEngine;
    
    private final AtomicBoolean scheduledTaskEnabled = new AtomicBoolean(true);
    private final Random random = new Random();
    
    // Using MockDataConstants for current prices (mutable copy)
    private static final Map<String, BigDecimal> CURRENT_PRICES = new HashMap<>(MockDataConstants.INITIAL_PRICES);
    
    @Override
    @Scheduled(fixedDelay = BusinessConstants.SCHEDULED_TASK_DELAY_MS)
    public void scheduledOrderBookMaintenance() {
        if (!scheduledTaskEnabled.get()) {
            return;
        }
        
        log.info(LogMessages.SCHEDULED_MAINTENANCE_START);
        
        try {
            updateAllMarketPrices();
            // processActiveUserOrders(); // KAPATTIK! Artık AlgorithmicMatching kullanıyoruz
            refreshDisplayOrderBooks();
            
            log.info(LogMessages.SCHEDULED_MAINTENANCE_SUCCESS);
        } catch (Exception e) {
            log.error(LogMessages.SCHEDULED_MAINTENANCE_ERROR, e.getMessage(), e);
        }
    }
    
    @Override
    public void updateAllMarketPrices() {
        log.info(LogMessages.PRICE_UPDATE_START);
        
        CURRENT_PRICES.forEach((symbol, currentPrice) -> {
            BigDecimal newPrice = simulatePriceMovement(symbol, currentPrice);
            updateSymbolPrice(symbol, newPrice);
        });
    }
    
    @Override
    public void processActiveUserOrders() {
        log.info(LogMessages.USER_ORDERS_PROCESSING);
        
        displayOrderBookService.getActiveDisplaySymbols().forEach(symbol -> {
            processUserOrdersForSymbol(symbol);
        });
    }
    
    @Override
    public void refreshDisplayOrderBooks() {
        log.info("🔄 Refreshing display order books with new prices");
        
        CURRENT_PRICES.forEach((symbol, price) -> {
            displayOrderBookService.maintainDisplayDepth(symbol, price);
        });
    }
    
    @Override
    public void processUserOrdersForSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return;
        }
        
        List<DisplayOrder> userOrders = getUserOrdersFromDisplay(symbol);
        if (userOrders.isEmpty()) {
            return;
        }
        
        log.debug("Processing {} user orders for symbol: {}", userOrders.size(), symbol);
        
        userOrders.forEach(displayOrder -> {
            tryMatchUserOrder(displayOrder, symbol);
        });
    }
    
    @Override
    public boolean isScheduledTaskEnabled() {
        return scheduledTaskEnabled.get();
    }
    
    @Override
    public void enableScheduledTask() {
        scheduledTaskEnabled.set(true);
        log.info(LogMessages.SCHEDULED_TASK_ENABLED);
    }
    
    @Override
    public void disableScheduledTask() {
        scheduledTaskEnabled.set(false);
        log.info(LogMessages.SCHEDULED_TASK_DISABLED);
    }
    
    private BigDecimal simulatePriceMovement(String symbol, BigDecimal currentPrice) {
        // Simulate ±0.5% price movement with volatility
        double volatilityPercent = getSymbolVolatility(symbol);
        double changePercent = (random.nextGaussian() * volatilityPercent) / 100.0;
        
        BigDecimal change = currentPrice.multiply(BigDecimal.valueOf(changePercent));
        BigDecimal newPrice = currentPrice.add(change);
        
        return newPrice.setScale(BusinessConstants.PRICE_SCALE - 1, RoundingMode.HALF_UP); // 2 decimal for prices
    }
    
    private double getSymbolVolatility(String symbol) {
        return MockDataConstants.SYMBOL_VOLATILITIES.getOrDefault(symbol, MockDataConstants.DEFAULT_VOLATILITY);
    }
    
    private void updateSymbolPrice(String symbol, BigDecimal newPrice) {
        // Update the price in CURRENT_PRICES map (mock implementation)
        CURRENT_PRICES.put(symbol, newPrice);
        log.debug("Price updated for {}: {}", symbol, newPrice);
    }
    
    private List<DisplayOrder> getUserOrdersFromDisplay(String symbol) {
        // Get user orders from DisplayOrderBookService
        return displayOrderBookService.getUserOrdersForSymbol(symbol);
    }
    
    private void tryMatchUserOrder(DisplayOrder displayOrder, String symbol) {
        if (displayOrder == null || displayOrder.isBot()) {
            return;
        }
        
        // Convert DisplayOrder to Order entity for matching
        Order userOrder = convertDisplayToOrder(displayOrder);
        BigDecimal currentPrice = CURRENT_PRICES.get(symbol);
        
        MatchingResult result = userOrderMatchingEngine.processUserOrder(userOrder, currentPrice);
        
        if (result.isSuccess() && result.hasMatched()) {
            handleSuccessfulMatch(displayOrder, result, symbol);
        }
    }
    
    private Order convertDisplayToOrder(DisplayOrder displayOrder) {
        Order order = new Order();
        order.setOrderId(displayOrder.getDisplayOrderId());
        order.setSymbol(displayOrder.getSymbol());
        order.setSide(displayOrder.getSide());
        order.setPrice(displayOrder.getPrice());
        order.setQuantity(displayOrder.getQuantity());
        order.setFilledQuantity(BigDecimal.ZERO);
        order.setIsBot(false);
        return order;
    }
    
    private void handleSuccessfulMatch(DisplayOrder displayOrder, MatchingResult result, String symbol) {
        log.info("🎯 User order matched in scheduled task: {} filled {} of {}", 
                result.getStrategy(), result.getFilledQuantity(), displayOrder.getQuantity());
        
        // Update display order quantity
        BigDecimal newQuantity = displayOrder.getQuantity().subtract(result.getFilledQuantity());
        
        if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            // Order completely filled - remove from display
            displayOrderBookService.removeFromDisplay(symbol, displayOrder.getDisplayOrderId());
            log.info("✅ User order completely filled and removed from display");
        } else {
            // Update remaining quantity
            displayOrder.setQuantity(newQuantity);
            log.info("📊 User order partially filled, {} remaining", newQuantity);
        }
    }
}