package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.Trade;
import com.stofina.app.orderservice.enums.OrderSide;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.enums.OrderType;
import com.stofina.app.orderservice.repository.OrderRepository;
import com.stofina.app.orderservice.repository.TradeRepository;
import com.stofina.app.orderservice.service.AlgorithmicMatchingService;
import com.stofina.app.orderservice.service.client.PortfolioClient;
import com.stofina.app.orderservice.dto.portfolio.BuyStockRequest;
import com.stofina.app.orderservice.dto.portfolio.SellStockRequest;
import com.stofina.app.orderservice.dto.portfolio.PortfolioResponse;
import com.stofina.app.orderservice.dto.portfolio.TradeConfirmationRequest;
import com.stofina.app.orderservice.dto.portfolio.PartialTradeConfirmationRequest;
import java.util.concurrent.CompletableFuture;
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
    
    // CHECKPOINT 3.3 - Portfolio Service integration for algorithmic orders
    private final PortfolioClient portfolioClient;
    
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
        
        // CHECKPOINT 3.3 - Check if bot order creation failed due to portfolio validation
        if (counterBotOrder == null) {
            log.warn("🤖 ALGORITHMIC: Counter bot order creation failed → UserOrderId: {}, returning empty trades", 
                    userOrder.getOrderId());
            return new ArrayList<>();
        }
        
        // Create trade
        Trade trade = createAlgorithmicTrade(userOrder, counterBotOrder, counterQuantity);
        
        // Update user order
        updateOrderAfterAlgorithmicTrade(userOrder, counterQuantity, userOrder.getPrice());
        
        // Save trade and updated order
        tradeRepository.save(trade);
        orderRepository.save(userOrder);
        
        log.info("Algorithmic trade executed: {} {} @ {} - Trade ID: {}", 
                counterQuantity, userOrder.getSymbol(), userOrder.getPrice(), trade.getTradeId());
        
        // AUTO-CONFIRM: Automatically confirm filled/partially filled user order in Portfolio Service
        if (userOrder.getStatus() == OrderStatus.FILLED) {
            log.info("🤖 AUTO-CONFIRM: User Order FILLED via algorithmic matching, confirming in Portfolio Service → OrderId: {}", userOrder.getOrderId());
            autoConfirmFilledOrder(userOrder, trade);
        } else if (userOrder.getStatus() == OrderStatus.PARTIALLY_FILLED) {
            log.info("🤖 AUTO-CONFIRM: User Order PARTIALLY_FILLED via algorithmic matching, confirming partial trade → OrderId: {}", userOrder.getOrderId());
            autoConfirmFilledOrder(userOrder, trade);
        }
        
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
        
        // CHECKPOINT 3.3 - Portfolio validation for algorithmic bot orders
        if (!validateAlgorithmicBotPortfolio(counterBot)) {
            log.warn("🤖 ALGORITHMIC: Bot portfolio validation failed for counter order → Symbol: {}, Side: {}, Quantity: {}", 
                    counterBot.getSymbol(), counterBot.getSide(), quantity);
            return null; // Return null if validation fails
        }
        
        // Save bot order to get auto-generated ID
        Order savedBot = orderRepository.save(counterBot);
        log.info("🤖 ALGORITHMIC: Generated counter bot order → BotOrderId: {}, Symbol: {}, Side: {}, Quantity: {}", 
                savedBot.getOrderId(), savedBot.getSymbol(), savedBot.getSide(), quantity);
        
        return savedBot;
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
        trade.setIsBotTrade(true); // Mark as algorithmic trade
        
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

    // CHECKPOINT 3.3 - Portfolio Service Integration Helper Methods

    /**
     * Validates that the algorithmic bot has sufficient portfolio capacity for the counter order.
     * For demo purposes, this assumes the bot account (999999L) has unlimited capacity.
     * In production, this would check actual bot account balances and positions.
     */
    private boolean validateAlgorithmicBotPortfolio(Order botOrder) {
        try {
            // Bot account validation - for demo, we assume unlimited capacity
            log.debug("🤖 ALGORITHMIC: Validating bot portfolio → AccountId: {}, Side: {}, Symbol: {}, Quantity: {}", 
                    botOrder.getAccountId(), botOrder.getSide(), botOrder.getSymbol(), botOrder.getQuantity());

            if (botOrder.getSide() == OrderSide.BUY) {
                // For buy orders, validate bot has sufficient balance
                return validateBotBalance(botOrder);
            } else {
                // For sell orders, validate bot has sufficient stock
                return validateBotStock(botOrder);
            }

        } catch (Exception e) {
            log.error("🤖 ALGORITHMIC: Portfolio validation error for bot order → Symbol: {}, Error: {}", 
                    botOrder.getSymbol(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validates bot account balance for buy orders.
     * In demo mode, always returns true (unlimited bot balance).
     * In production, would call Portfolio Service for actual validation.
     */
    private boolean validateBotBalance(Order botBuyOrder) {
        BigDecimal requiredAmount = botBuyOrder.getPrice().multiply(botBuyOrder.getQuantity());
        
        log.debug("🤖 ALGORITHMIC: Bot balance validation → Required: {}, Symbol: {}", 
                requiredAmount, botBuyOrder.getSymbol());

        // Demo Mode: Assume bot has unlimited balance
        // TODO: In production, implement actual Portfolio Service call:
        /*
        try {
            CompletableFuture<PortfolioResponse> validationFuture = portfolioClient.validateAccountBalance(
                    botBuyOrder.getAccountId(), requiredAmount
            );
            PortfolioResponse response = validationFuture.get();
            return response.isSuccess();
        } catch (Exception e) {
            log.error("🤖 ALGORITHMIC: Bot balance validation failed", e);
            return false;
        }
        */

        // For now, always return true for demo
        return true;
    }

    /**
     * Validates bot account stock position for sell orders.
     * In demo mode, always returns true (unlimited bot stock).
     * In production, would call Portfolio Service for actual validation.
     */
    private boolean validateBotStock(Order botSellOrder) {
        log.debug("🤖 ALGORITHMIC: Bot stock validation → Symbol: {}, Quantity: {}", 
                botSellOrder.getSymbol(), botSellOrder.getQuantity());

        // Demo Mode: Assume bot has unlimited stock positions
        // TODO: In production, implement actual Portfolio Service call:
        /*
        try {
            CompletableFuture<PortfolioResponse> validationFuture = portfolioClient.validateStockPosition(
                    botSellOrder.getAccountId(), 
                    botSellOrder.getSymbol(), 
                    botSellOrder.getQuantity().intValue()
            );
            PortfolioResponse response = validationFuture.get();
            return response.isSuccess();
        } catch (Exception e) {
            log.error("🤖 ALGORITHMIC: Bot stock validation failed", e);
            return false;
        }
        */

        // For now, always return true for demo
        return true;
    }
    
    /**
     * AUTO-CONFIRM: Automatically confirm filled order in Portfolio Service
     * This method is called when an order is marked as FILLED or PARTIALLY_FILLED via algorithmic matching
     * to automatically call the appropriate Portfolio Service confirm endpoint.
     */
    private void autoConfirmFilledOrder(Order filledOrder, Trade trade) {
        try {
            log.info("🤖 AUTO-CONFIRM (ALGORITHMIC): Processing order → OrderId: {}, Side: {}, Status: {}", 
                    filledOrder.getOrderId(), filledOrder.getSide(), filledOrder.getStatus());
            
            // Skip auto-confirm for bot orders (account ID is 999999L)
            if (filledOrder.getAccountId().equals(999999L)) {
                log.info("🤖 AUTO-CONFIRM (ALGORITHMIC): Skipping auto-confirm for BOT account → OrderId: {}", filledOrder.getOrderId());
                return;
            }
            
            CompletableFuture<PortfolioResponse> confirmationFuture;
            
            if (filledOrder.getSide() == OrderSide.BUY) {
                if (filledOrder.getStatus() == OrderStatus.FILLED) {
                    // FULLY FILLED - Use normal confirmBuyTrade
                    TradeConfirmationRequest buyRequest = TradeConfirmationRequest.builder()
                            .tradeId(trade.getTradeId())
                            .orderId(filledOrder.getOrderId())
                            .executedQuantity(trade.getQuantity().intValue())  // Trade quantity
                            .executedPrice(trade.getPrice())
                            .build();
                    
                    log.info("🤖 AUTO-CONFIRM (ALGORITHMIC): Confirming FULLY FILLED BUY order → {}", buyRequest);
                    confirmationFuture = portfolioClient.confirmBuyTrade(buyRequest);
                } else if (filledOrder.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                    // PARTIALLY FILLED - Use confirmPartialBuyTrade
                    PartialTradeConfirmationRequest partialBuyRequest = PartialTradeConfirmationRequest.builder()
                            .tradeId(trade.getTradeId())
                            .orderId(filledOrder.getOrderId())
                            .partialQuantity(trade.getQuantity().intValue())  // Actually filled quantity
                            .remainingQuantity(filledOrder.getRemainingQuantity().intValue())
                            .executedPrice(trade.getPrice())
                            .build();
                    
                    log.info("🤖 AUTO-CONFIRM (ALGORITHMIC): Confirming PARTIALLY FILLED BUY order → {}", partialBuyRequest);
                    confirmationFuture = portfolioClient.confirmPartialBuyTrade(partialBuyRequest);
                } else {
                    log.warn("🤖 AUTO-CONFIRM (ALGORITHMIC): Unexpected order status for BUY order → OrderId: {}, Status: {}", 
                            filledOrder.getOrderId(), filledOrder.getStatus());
                    return;
                }
            } else {
                if (filledOrder.getStatus() == OrderStatus.FILLED) {
                    // FULLY FILLED - Use normal confirmSellTrade
                    TradeConfirmationRequest sellRequest = TradeConfirmationRequest.builder()
                            .tradeId(trade.getTradeId())
                            .orderId(filledOrder.getOrderId())
                            .executedQuantity(trade.getQuantity().intValue())  // Trade quantity
                            .executedPrice(trade.getPrice())
                            .build();
                    
                    log.info("🤖 AUTO-CONFIRM (ALGORITHMIC): Confirming FULLY FILLED SELL order → {}", sellRequest);
                    confirmationFuture = portfolioClient.confirmSellTrade(sellRequest);
                } else if (filledOrder.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                    // PARTIALLY FILLED - Use confirmPartialSellTrade
                    PartialTradeConfirmationRequest partialSellRequest = PartialTradeConfirmationRequest.builder()
                            .tradeId(trade.getTradeId())
                            .orderId(filledOrder.getOrderId())
                            .partialQuantity(trade.getQuantity().intValue())  // Actually filled quantity
                            .remainingQuantity(filledOrder.getRemainingQuantity().intValue())
                            .executedPrice(trade.getPrice())
                            .build();
                    
                    log.info("🤖 AUTO-CONFIRM (ALGORITHMIC): Confirming PARTIALLY FILLED SELL order → {}", partialSellRequest);
                    confirmationFuture = portfolioClient.confirmPartialSellTrade(partialSellRequest);
                } else {
                    log.warn("🤖 AUTO-CONFIRM (ALGORITHMIC): Unexpected order status for SELL order → OrderId: {}, Status: {}", 
                            filledOrder.getOrderId(), filledOrder.getStatus());
                    return;
                }
            }
            
            // Execute confirmation asynchronously without blocking
            confirmationFuture.thenAccept(response -> {
                if (response.isSuccess()) {
                    log.info("✅ AUTO-CONFIRM (ALGORITHMIC): Order confirmation successful → OrderId: {}", filledOrder.getOrderId());
                } else {
                    log.error("❌ AUTO-CONFIRM (ALGORITHMIC): Order confirmation failed → OrderId: {}, Error: {}", 
                            filledOrder.getOrderId(), response.getMessage());
                }
            }).exceptionally(throwable -> {
                log.error("🚨 AUTO-CONFIRM (ALGORITHMIC): Exception during order confirmation → OrderId: {}, Error: {}", 
                        filledOrder.getOrderId(), throwable.getMessage());
                return null;
            });
            
        } catch (Exception e) {
            log.error("🚨 AUTO-CONFIRM (ALGORITHMIC): Unexpected error during auto-confirm → OrderId: {}, Error: {}", 
                    filledOrder.getOrderId(), e.getMessage());
        }
    }
}