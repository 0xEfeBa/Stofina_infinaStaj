package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.Trade;
import com.stofina.app.orderservice.enums.OrderSide;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.enums.OrderType;
import com.stofina.app.orderservice.repository.OrderRepository;
import com.stofina.app.orderservice.repository.TradeRepository;
import com.stofina.app.orderservice.service.AlgorithmicMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlgorithmicMatchingServiceImpl implements AlgorithmicMatchingService {
    
    // CHECKPOINT C1 - Algorithmic Matching Service Implementation
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final Random random = new Random();
    
    // Order tracking: orderId -> algorithmic matching count (max 2)
    private final Map<Long, Integer> algorithmicMatchingCounts = new ConcurrentHashMap<>();
    
    // Scheduled executor for delayed algorithmic matching
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    
    // Algorithm strategies with probabilities
    private enum AlgorithmicStrategy {
        FULL_FILL(30),    // 30% - Generate full counter-order
        PARTIAL_FILL(40), // 40% - Generate partial counter-order
        NO_FILL(30);      // 30% - No matching
        
        private final int probability;
        
        AlgorithmicStrategy(int probability) {
            this.probability = probability;
        }
        
        public int getProbability() {
            return probability;
        }
    }
    
    @Override
    public void scheduleAlgorithmicMatching(Order order, int delaySeconds) {
        log.info("💡 LIFECYCLE-4: AlgorithmicMatchingService.scheduleAlgorithmicMatching() - ENTRY - OrderId={}, DelaySeconds={}", 
                 order.getOrderId(), delaySeconds);
        
        if (!isEligibleForAlgorithmicMatching(order.getOrderId())) {
            log.warn("💡 LIFECYCLE-4: AlgorithmicMatchingService - Order {} NOT ELIGIBLE (already processed 2 times)", 
                    order.getOrderId());
            return;
        }
        
        log.info("💡 LIFECYCLE-4: AlgorithmicMatchingService - ELIGIBLE - Scheduling execution in {} seconds", delaySeconds);
        
        scheduler.schedule(() -> {
            try {
                log.info("💡 LIFECYCLE-4: AlgorithmicMatchingService - DELAYED EXECUTION STARTED - OrderId={}", order.getOrderId());
                List<Trade> trades = executeAlgorithmicMatching(order);
                log.info("💡 LIFECYCLE-4: AlgorithmicMatchingService - DELAYED EXECUTION COMPLETED - OrderId={}, Trades={}", 
                         order.getOrderId(), trades.size());
            } catch (Exception e) {
                log.error("💡 LIFECYCLE-4: AlgorithmicMatchingService - EXECUTION FAILED - OrderId={}, Error: {}", 
                          order.getOrderId(), e.getMessage(), e);
            }
        }, delaySeconds, TimeUnit.SECONDS);
        
        log.info("💡 LIFECYCLE-4: AlgorithmicMatchingService.scheduleAlgorithmicMatching() - EXIT - Scheduler task submitted");
    }
    
    @Override
    public List<Trade> executeAlgorithmicMatching(Order order) {
        if (!isEligibleForAlgorithmicMatching(order.getOrderId())) {
            log.warn("Order {} not eligible for algorithmic matching", order.getOrderId());
            return new ArrayList<>();
        }
        
        // Increment algorithmic matching count
        int currentCount = algorithmicMatchingCounts.getOrDefault(order.getOrderId(), 0);
        algorithmicMatchingCounts.put(order.getOrderId(), currentCount + 1);
        
        log.info("Executing algorithmic matching for order {} (attempt {}/2)", 
                order.getOrderId(), currentCount + 1);
        
        // Get fresh order data from database
        Optional<Order> freshOrderOpt = orderRepository.findById(order.getOrderId());
        if (freshOrderOpt.isEmpty() || freshOrderOpt.get().getStatus() == OrderStatus.FILLED) {
            log.info("Order {} already filled or not found, skipping algorithmic matching", 
                    order.getOrderId());
            return new ArrayList<>();
        }
        
        Order freshOrder = freshOrderOpt.get();
        
        // Select algorithmic strategy
        AlgorithmicStrategy strategy = selectAlgorithmicStrategy();
        log.info("Selected algorithmic strategy: {} for order {}", strategy, freshOrder.getOrderId());
        
        switch (strategy) {
            case FULL_FILL:
                return executeFillStrategy(freshOrder, true);
            case PARTIAL_FILL:
                return executeFillStrategy(freshOrder, false);
            case NO_FILL:
                return executeNoFillStrategy(freshOrder);
            default:
                return new ArrayList<>();
        }
    }
    
    @Override
    public boolean isEligibleForAlgorithmicMatching(Long orderId) {
        int count = algorithmicMatchingCounts.getOrDefault(orderId, 0);
        return count < 2; // Maximum 2 algorithmic matching attempts per order
    }
    
    @Override
    public int getAlgorithmicMatchingCount(Long orderId) {
        return algorithmicMatchingCounts.getOrDefault(orderId, 0);
    }
    
    @Override
    public List<Trade> triggerAlgorithmicMatching(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            log.warn("Order {} not found for manual algorithmic matching trigger", orderId);
            return new ArrayList<>();
        }
        
        return executeAlgorithmicMatching(orderOpt.get());
    }
    
    private AlgorithmicStrategy selectAlgorithmicStrategy() {
        int randomValue = random.nextInt(100) + 1; // 1-100
        
        if (randomValue <= AlgorithmicStrategy.FULL_FILL.getProbability()) {
            return AlgorithmicStrategy.FULL_FILL;
        } else if (randomValue <= AlgorithmicStrategy.FULL_FILL.getProbability() + 
                   AlgorithmicStrategy.PARTIAL_FILL.getProbability()) {
            return AlgorithmicStrategy.PARTIAL_FILL;
        } else {
            return AlgorithmicStrategy.NO_FILL;
        }
    }
    
    private List<Trade> executeFillStrategy(Order userOrder, boolean fullFill) {
        BigDecimal counterQuantity;
        
        if (fullFill) {
            counterQuantity = userOrder.getRemainingQuantity();
            log.info("Executing FULL_FILL: {} remaining quantity", counterQuantity);
        } else {
            // Partial fill: 30-80% of remaining quantity
            double fillPercentage = 0.3 + (random.nextDouble() * 0.5); // 30-80%
            counterQuantity = userOrder.getRemainingQuantity()
                    .multiply(BigDecimal.valueOf(fillPercentage))
                    .setScale(0, RoundingMode.DOWN);
            
            if (counterQuantity.compareTo(BigDecimal.ONE) < 0) {
                counterQuantity = BigDecimal.ONE; // Minimum 1 quantity
            }
            
            log.info("Executing PARTIAL_FILL: {} of {} remaining ({}%)", 
                    counterQuantity, userOrder.getRemainingQuantity(), 
                    (int)(fillPercentage * 100));
        }
        
        // Generate counter-bot order
        Order counterBotOrder = generateCounterBotOrder(userOrder, counterQuantity);
        
        // Create trade
        Trade trade = createAlgorithmicTrade(userOrder, counterBotOrder, counterQuantity);
        
        // Update user order
        updateOrderAfterAlgorithmicTrade(userOrder, counterQuantity, userOrder.getPrice());
        
        // Save trade and updated order
        tradeRepository.save(trade);
        orderRepository.save(userOrder);
        
        log.info("Algorithmic trade executed: {} {} @ {} - Trade ID: {}", 
                counterQuantity, userOrder.getSymbol(), userOrder.getPrice(), trade.getTradeId());
        
        // Schedule next algorithmic matching if partially filled and eligible
        if (!fullFill && userOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0 
            && isEligibleForAlgorithmicMatching(userOrder.getOrderId())) {
            // Second attempt is always 15 seconds delay
            log.info("Scheduling second algorithmic matching for remaining quantity: {} (15 seconds delay)", 
                    userOrder.getRemainingQuantity());
            scheduleAlgorithmicMatching(userOrder, 15);
        }
        
        return Arrays.asList(trade);
    }
    
    private List<Trade> executeNoFillStrategy(Order userOrder) {
        log.info("Executing NO_FILL strategy for order {}: no matching, order stays in book", 
                userOrder.getOrderId());
        
        // Schedule next algorithmic matching if eligible
        if (isEligibleForAlgorithmicMatching(userOrder.getOrderId())) {
            // Second attempt is always 15 seconds delay
            log.info("Scheduling second algorithmic matching attempt for order {} (15 seconds delay)", 
                    userOrder.getOrderId());
            scheduleAlgorithmicMatching(userOrder, 15);
        } else {
            log.info("Order {} reached maximum algorithmic matching attempts, will remain in book", 
                    userOrder.getOrderId());
        }
        
        return new ArrayList<>(); // No trades generated
    }
    
    private Order generateCounterBotOrder(Order userOrder, BigDecimal quantity) {
        Order counterBot = new Order();
        // JPA will auto-generate the orderId - no manual setting needed
        counterBot.setTenantId(userOrder.getTenantId());
        counterBot.setAccountId(999999L); // Special algorithmic bot account
        counterBot.setSymbol(userOrder.getSymbol());
        counterBot.setOrderType(getOppositeOrderType(userOrder.getSide()));
        counterBot.setSide(getOppositeSide(userOrder.getSide()));
        counterBot.setPrice(userOrder.getPrice()); // Match at user's price
        counterBot.setQuantity(quantity);
        counterBot.setFilledQuantity(BigDecimal.ZERO);
        counterBot.setIsBot(true);
        counterBot.setStatus(OrderStatus.NEW);
        counterBot.setCreatedAt(LocalDateTime.now());
        
        // Save bot order to get auto-generated ID
        return orderRepository.save(counterBot);
    }
    
    private Trade createAlgorithmicTrade(Order userOrder, Order botOrder, BigDecimal quantity) {
        Order buyOrder = userOrder.getSide() == OrderSide.BUY ? userOrder : botOrder;
        Order sellOrder = userOrder.getSide() == OrderSide.SELL ? userOrder : botOrder;
        
        Trade trade = new Trade();
        // JPA will auto-generate the tradeId - no manual setting needed
        trade.setBuyOrderId(buyOrder.getOrderId());
        trade.setSellOrderId(sellOrder.getOrderId());
        trade.setSymbol(userOrder.getSymbol());
        trade.setPrice(userOrder.getPrice());
        trade.setQuantity(quantity);
        trade.setExecutedAt(LocalDateTime.now());
        trade.setBuyAccountId(buyOrder.getAccountId());
        trade.setSellAccountId(sellOrder.getAccountId());
        trade.setTenantId(userOrder.getTenantId());
        trade.setBotTrade(true); // Mark as algorithmic trade
        
        return trade;
    }
    
    private void updateOrderAfterAlgorithmicTrade(Order order, BigDecimal tradeQuantity, BigDecimal tradePrice) {
        BigDecimal newFilledQuantity = order.getFilledQuantity().add(tradeQuantity);
        order.setFilledQuantity(newFilledQuantity);
        
        // Update average price (weighted average)
        if (order.getAveragePrice() == null || order.getAveragePrice().compareTo(BigDecimal.ZERO) == 0) {
            order.setAveragePrice(tradePrice);
        } else {
            BigDecimal totalValue = order.getAveragePrice().multiply(order.getFilledQuantity().subtract(tradeQuantity))
                    .add(tradePrice.multiply(tradeQuantity));
            order.setAveragePrice(totalValue.divide(newFilledQuantity, 4, RoundingMode.HALF_UP));
        }
        
        // Update status based on remaining quantity calculation
        BigDecimal remainingQuantity = order.getQuantity().subtract(newFilledQuantity);
        if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            order.setStatus(OrderStatus.FILLED);
        } else {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        }
        
        order.setUpdatedAt(LocalDateTime.now());
    }
    
    private OrderSide getOppositeSide(OrderSide side) {
        return side == OrderSide.BUY ? OrderSide.SELL : OrderSide.BUY;
    }
    
    private OrderType getOppositeOrderType(OrderSide userSide) {
        return userSide == OrderSide.BUY ? OrderType.LIMIT_SELL : OrderType.LIMIT_BUY;
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down algorithmic matching scheduler...");
        scheduler.shutdown();
    }
}