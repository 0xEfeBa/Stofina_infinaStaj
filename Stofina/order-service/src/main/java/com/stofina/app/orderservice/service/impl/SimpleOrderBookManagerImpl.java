package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.Trade;
import com.stofina.app.orderservice.model.OrderLevel;
import com.stofina.app.orderservice.enums.OrderSide;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.model.SimpleOrderBook;
import com.stofina.app.orderservice.model.SimpleOrderBookSnapshot;
import com.stofina.app.orderservice.repository.OrderRepository;
import com.stofina.app.orderservice.repository.TradeRepository;
import com.stofina.app.orderservice.service.AlgorithmicMatchingService;
import com.stofina.app.orderservice.service.SimpleOrderBookManager;
import com.stofina.app.orderservice.service.client.PortfolioClient;
import com.stofina.app.orderservice.service.CompensationService;
import com.stofina.app.orderservice.dto.portfolio.*;
import com.stofina.app.orderservice.exception.portfolio.PortfolioServiceException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleOrderBookManagerImpl implements SimpleOrderBookManager {
    
    // CHECKPOINT 5.2 - Thread-Safe Order Book Management
    private final ConcurrentHashMap<String, SimpleOrderBook> orderBooks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> symbolLocks = new ConcurrentHashMap<>();
    
    // CHECKPOINT ENTEGRASYON 2.2 - Repository dependencies
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    
    // CHECKPOINT C3 - Algorithmic matching integration
    private final AlgorithmicMatchingService algorithmicMatchingService;
    
    // CHECKPOINT 2.3 - Portfolio Service integration
    private final PortfolioClient portfolioClient;
    
    // CHECKPOINT 3.2 - Compensation Service integration
    private final CompensationService compensationService;
    
    // TODO: ENTEGRASYON SIRASINDA KALDIRILACAK - Mock BIST symbols for testing
    private static final List<String> MOCK_BIST_SYMBOLS = Arrays.asList(
        "AKBNK", "CCOLA", "DOAS", "MGROS", "FROTO",
        "TCELL", "THYAO", "YEOTK", "BRSAN", "TUPRS"
    );
    
    @PostConstruct
    public void initializeMockSymbols() {
        // TODO: ENTEGRASYON SIRASINDA KALDIRILACAK - Initialize mock BIST symbols
        log.info("Initializing mock BIST symbols for testing...");
        MOCK_BIST_SYMBOLS.forEach(symbol -> {
            initializeOrderBook(symbol);
            log.debug("Mock order book initialized for symbol: {}", symbol);
        });
        log.info("Mock BIST symbols initialized: {}", MOCK_BIST_SYMBOLS.size());
    }
    
    @Override
    public void initializeOrderBook(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        orderBooks.computeIfAbsent(normalizedSymbol, SimpleOrderBook::new);
        symbolLocks.computeIfAbsent(normalizedSymbol, k -> new ReentrantLock());
        
        log.debug("Order book initialized for symbol: {}", normalizedSymbol);
    }
    
    @Override
    public List<Trade> addOrder(Order order) {
        log.info("⚡ LIFECYCLE-3: SimpleOrderBookManager.addOrder() - ENTRY - OrderId={}, Symbol={}, Side={}, Quantity={}, Price={}", 
                 order.getOrderId(), order.getSymbol(), order.getSide(), order.getQuantity(), order.getPrice());
        
        if (order == null || order.getSymbol() == null) {
            log.warn("⚡ LIFECYCLE-3: SimpleOrderBookManager - NULL ORDER OR SYMBOL - EARLY RETURN");
            return new ArrayList<>();
        }
        
        String symbol = order.getSymbol().trim().toUpperCase();
        ReentrantLock lock = acquireLock(symbol);
        
        try {
            // CHECKPOINT ENTEGRASYON 2.3 - Matching before adding
            log.info("⚡ LIFECYCLE-3: SimpleOrderBookManager - Attempting immediate matching for order {}", order.getOrderId());
            List<Trade> trades = matchOrder(order);
            log.info("⚡ LIFECYCLE-3: SimpleOrderBookManager - Immediate matching result: {} trades found", trades.size());
            
            // If order not fully filled, add remaining to book and trigger algorithmic matching
            log.info("🔍 DEBUG-A: Checking remaining quantity = {}", order.getRemainingQuantity());
            if (order.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
                log.info("DEBUG - Order has remaining quantity: {}", order.getRemainingQuantity());
                SimpleOrderBook orderBook = getOrCreateOrderBook(symbol);
                orderBook.addOrder(order);
                log.info("Order added to book: {} for symbol: {} with remaining quantity: {}", 
                         order.getOrderId(), symbol, order.getRemainingQuantity());
                
                // CHECKPOINT C3 - Trigger algorithmic matching for unfilled order
                boolean eligible = algorithmicMatchingService.isEligibleForAlgorithmicMatching(order.getOrderId());
                log.info("🔍 DEBUG-B: Eligibility for order {} = {}", order.getOrderId(), eligible);
                if (eligible) {
                    // First attempt: 3 seconds, Second attempt: 15 seconds
                    int attemptCount = algorithmicMatchingService.getAlgorithmicMatchingCount(order.getOrderId());
                    int delaySeconds = (attemptCount == 0) ? 3 : 15;
                    
                    log.info("Triggering algorithmic matching for unfilled order: {} (remaining: {}, attempt {}/2, delay: {} seconds)", 
                            order.getOrderId(), order.getRemainingQuantity(), attemptCount + 1, delaySeconds);
                    algorithmicMatchingService.scheduleAlgorithmicMatching(order, delaySeconds);
                } else {
                    log.info("Order {} not eligible for algorithmic matching (max 2 attempts reached)", 
                            order.getOrderId());
                }
            }
            
            return trades;
        } catch (Exception e) {
            log.error("Failed to process order {} for symbol {}: {}", order.getOrderId(), symbol, e.getMessage());
            return new ArrayList<>();
        } finally {
            releaseLock(lock);
        }
    }
    
    @Override
    public boolean removeOrder(Long orderId, String symbol) {
        if (orderId == null || symbol == null) {
            return false;
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        ReentrantLock lock = acquireLock(normalizedSymbol);
        
        try {
            SimpleOrderBook orderBook = orderBooks.get(normalizedSymbol);
            if (orderBook == null) {
                return false;
            }
            
            boolean removed = orderBook.removeOrder(orderId);
            if (removed) {
                log.debug("Order removed: {} from symbol: {}", orderId, normalizedSymbol);
            }
            return removed;
        } finally {
            releaseLock(lock);
        }
    }
    
    @Override
    public boolean updateOrder(Order oldOrder, Order newOrder) {
        if (oldOrder == null || newOrder == null) {
            return false;
        }
        
        String symbol = oldOrder.getSymbol().trim().toUpperCase();
        ReentrantLock lock = acquireLock(symbol);
        
        try {
            boolean removed = removeOrderWithoutLock(oldOrder.getOrderId(), symbol);
            if (removed) {
                SimpleOrderBook orderBook = getOrCreateOrderBook(symbol);
                orderBook.addOrder(newOrder);
                return true;
            }
            return false;
        } finally {
            releaseLock(lock);
        }
    }
    
    @Override
    public SimpleOrderBook getOrderBook(String symbol) {
        if (symbol == null) {
            return null;
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        return orderBooks.get(normalizedSymbol);
    }
    
    @Override
    public SimpleOrderBookSnapshot getOrderBookSnapshot(String symbol) {
        if (symbol == null) {
            return null;
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        ReentrantLock lock = acquireLock(normalizedSymbol);
        
        try {
            SimpleOrderBook orderBook = orderBooks.get(normalizedSymbol);
            if (orderBook == null) {
                return createEmptySnapshot(normalizedSymbol);
            }
            
            return createSnapshot(orderBook);
        } finally {
            releaseLock(lock);
        }
    }
    
    @Override
    public BigDecimal getBestBid(String symbol) {
        SimpleOrderBook orderBook = getOrderBook(symbol);
        return orderBook != null ? orderBook.getBestBid() : null;
    }
    
    @Override
    public BigDecimal getBestAsk(String symbol) {
        SimpleOrderBook orderBook = getOrderBook(symbol);
        return orderBook != null ? orderBook.getBestAsk() : null;
    }
    
    @Override
    public BigDecimal getSpread(String symbol) {
        SimpleOrderBook orderBook = getOrderBook(symbol);
        return orderBook != null ? orderBook.getSpread() : null;
    }
    
    @Override
    public void clearOrderBook(String symbol) {
        if (symbol == null) {
            return;
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        ReentrantLock lock = acquireLock(normalizedSymbol);
        
        try {
            orderBooks.remove(normalizedSymbol);
            initializeOrderBook(normalizedSymbol);
            log.debug("Order book cleared for symbol: {}", normalizedSymbol);
        } finally {
            releaseLock(lock);
        }
    }
    
    @Override
    public Set<String> getActiveSymbols() {
        return orderBooks.keySet();
    }
    
    @Override
    public int getTotalOrderCount(String symbol) {
        SimpleOrderBook orderBook = getOrderBook(symbol);
        return orderBook != null ? orderBook.getTotalOrderCount() : 0;
    }
    
    @Override
    public boolean isSymbolActive(String symbol) {
        return symbol != null && orderBooks.containsKey(symbol.trim().toUpperCase());
    }
    
    private ReentrantLock acquireLock(String symbol) {
        ReentrantLock lock = symbolLocks.computeIfAbsent(symbol, k -> new ReentrantLock());
        lock.lock();
        return lock;
    }
    
    private void releaseLock(ReentrantLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
    
    private SimpleOrderBook getOrCreateOrderBook(String symbol) {
        return orderBooks.computeIfAbsent(symbol, SimpleOrderBook::new);
    }
    
    private boolean removeOrderWithoutLock(Long orderId, String symbol) {
        SimpleOrderBook orderBook = orderBooks.get(symbol);
        return orderBook != null && orderBook.removeOrder(orderId);
    }
    
    private SimpleOrderBookSnapshot createSnapshot(SimpleOrderBook orderBook) {
        return new SimpleOrderBookSnapshot(
            orderBook.getSymbol(),
            orderBook.getTop10Bids(),
            orderBook.getTop10Asks(),
            orderBook.getBestBid(),
            orderBook.getBestAsk(),
            orderBook.getSpread(),
            orderBook.getLastUpdateTime(),
            calculateTotalQuantity(orderBook.getTop10Bids()),
            calculateTotalQuantity(orderBook.getTop10Asks())
        );
    }
    
    private SimpleOrderBookSnapshot createEmptySnapshot(String symbol) {
        return new SimpleOrderBookSnapshot(
            symbol, null, null, null, null, null, null, 0, 0
        );
    }
    
    private int calculateTotalQuantity(java.util.List<OrderLevel> levels) {
        return levels != null ? 
            levels.stream().mapToInt(level -> level.getQuantity().intValue()).sum() : 0;
    }
    
    // CHECKPOINT ENTEGRASYON 2.4 - Real Order Book Matching Implementation
    @Override
    public List<Trade> matchOrder(Order newOrder) {
        List<Trade> trades = new ArrayList<>();
        if (newOrder == null || newOrder.getSymbol() == null) {
            return trades;
        }
        
        String symbol = newOrder.getSymbol().trim().toUpperCase();
        SimpleOrderBook orderBook = orderBooks.get(symbol);
        if (orderBook == null) {
            orderBook = getOrCreateOrderBook(symbol);
        }
        
        List<Order> matchingOrders = getMatchingOrders(newOrder);
        
        for (Order oppositeOrder : matchingOrders) {
            if (newOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                break; // New order fully filled
            }
            
            if (oppositeOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue; // Skip already filled orders
            }
            
            // Execute trade
            BigDecimal tradeQuantity = newOrder.getRemainingQuantity().min(oppositeOrder.getRemainingQuantity());
            BigDecimal tradePrice = oppositeOrder.getPrice(); // Price from existing order (price-time priority)
            
            Trade trade = createTrade(newOrder, oppositeOrder, tradePrice, tradeQuantity);
            
            // CHECKPOINT 2.3 - Portfolio Service Trade Confirmation BEFORE updating order state
            log.info("🏦 PORTFOLIO: Starting trade confirmation → TradeId: {}, NewOrder: {}, OppositeOrder: {}", 
                    trade.getTradeId(), newOrder.getOrderId(), oppositeOrder.getOrderId());
            
            boolean portfolioConfirmationSuccess = performPortfolioTradeConfirmation(trade, newOrder, oppositeOrder, tradeQuantity);
            if (!portfolioConfirmationSuccess) {
                log.error("🏦 PORTFOLIO: Trade confirmation failed → TradeId: {}, attempting compensation", 
                        trade.getTradeId());
                
                // CHECKPOINT 3.2 - Trigger compensation for failed trade confirmation
                try {
                    Order buyOrder = newOrder.getSide() == OrderSide.BUY ? newOrder : oppositeOrder;
                    Order sellOrder = newOrder.getSide() == OrderSide.SELL ? newOrder : oppositeOrder;
                    
                    boolean compensationSuccess = compensationService.compensateFailedTrade(
                            trade, buyOrder, sellOrder, "Portfolio trade confirmation failed"
                    );
                    
                    if (compensationSuccess) {
                        log.info("✅ COMPENSATION: Trade compensation successful → TradeId: {}", trade.getTradeId());
                    } else {
                        log.error("❌ COMPENSATION: Trade compensation failed → TradeId: {}", trade.getTradeId());
                    }
                } catch (Exception compensationEx) {
                    log.error("🚨 COMPENSATION: Exception during trade compensation → TradeId: {}", 
                            trade.getTradeId(), compensationEx);
                }
                
                continue; // Skip this trade, try next matching order
            }
            
            trades.add(trade);
            
            // Update order quantities AFTER portfolio confirmation
            updateOrderAfterTrade(newOrder, tradeQuantity, tradePrice);
            updateOrderAfterTrade(oppositeOrder, tradeQuantity, tradePrice);
            
            // Save trade to database
            tradeRepository.save(trade);
            
            // Update orders in database
            orderRepository.save(newOrder);
            orderRepository.save(oppositeOrder);
            
            // Remove fully filled orders from book
            if (oppositeOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                orderBook.removeOrder(oppositeOrder.getOrderId());
                oppositeOrder.setStatus(OrderStatus.FILLED);
                orderRepository.save(oppositeOrder);
                
                // AUTO-CONFIRM: Automatically confirm filled order in Portfolio Service
                log.info("🔄 AUTO-CONFIRM: Order FILLED, confirming in Portfolio Service → OrderId: {}", oppositeOrder.getOrderId());
                autoConfirmFilledOrder(oppositeOrder, trade);
            }
            
            log.info("Trade executed: {} {} @ {} between orders {} and {}", 
                    tradeQuantity, symbol, tradePrice, newOrder.getOrderId(), oppositeOrder.getOrderId());
        }
        
        // Update new order status
        if (newOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            newOrder.setStatus(OrderStatus.FILLED);
            
            // AUTO-CONFIRM: Automatically confirm filled new order in Portfolio Service
            log.info("🔄 AUTO-CONFIRM: New Order FILLED, confirming in Portfolio Service → OrderId: {}", newOrder.getOrderId());
            // Use the last trade for this new order
            if (!trades.isEmpty()) {
                Trade lastTrade = trades.get(trades.size() - 1);
                autoConfirmFilledOrder(newOrder, lastTrade);
            }
        } else if (newOrder.getFilledQuantity().compareTo(BigDecimal.ZERO) > 0) {
            newOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
            
            // AUTO-CONFIRM: PARTIALLY_FILLED emirler için de otomatik onay
            log.info("🔄 AUTO-CONFIRM: New Order PARTIALLY_FILLED, confirming partial trades → OrderId: {}", newOrder.getOrderId());
            
            // PARTIALLY_FILLED emirlerde tüm trade'leri onayla
            for (Trade trade : trades) {
                if (trade.getBuyOrderId().equals(newOrder.getOrderId()) || 
                    trade.getSellOrderId().equals(newOrder.getOrderId())) {
                    autoConfirmFilledOrder(newOrder, trade);
                }
            }
        }
        
        return trades;
    }
    
    @Override
    public List<Order> getMatchingOrders(Order newOrder) {
        List<Order> matchingOrders = new ArrayList<>();
        if (newOrder == null || newOrder.getSymbol() == null) {
            return matchingOrders;
        }
        
        String symbol = newOrder.getSymbol().trim().toUpperCase();
        SimpleOrderBook orderBook = orderBooks.get(symbol);
        if (orderBook == null) {
            return matchingOrders;
        }
        
        // Get orders from opposite side
        List<Order> oppositeOrders = newOrder.getSide() == OrderSide.BUY ? 
                                    orderBook.getAskOrders() : orderBook.getBidOrders();
        
        for (Order oppositeOrder : oppositeOrders) {
            if (canMatch(newOrder, oppositeOrder)) {
                matchingOrders.add(oppositeOrder);
            }
        }
        
        // Sort by price-time priority
        if (newOrder.getSide() == OrderSide.BUY) {
            // For buy orders, match with lowest ask prices first
            matchingOrders.sort((o1, o2) -> {
                int priceCompare = o1.getPrice().compareTo(o2.getPrice());
                return priceCompare != 0 ? priceCompare : o1.getCreatedAt().compareTo(o2.getCreatedAt());
            });
        } else {
            // For sell orders, match with highest bid prices first
            matchingOrders.sort((o1, o2) -> {
                int priceCompare = o2.getPrice().compareTo(o1.getPrice());
                return priceCompare != 0 ? priceCompare : o1.getCreatedAt().compareTo(o2.getCreatedAt());
            });
        }
        
        return matchingOrders;
    }
    
    private boolean canMatch(Order buyOrder, Order sellOrder) {
        if (buyOrder.getSide() == sellOrder.getSide()) {
            return false; // Same side orders cannot match
        }
        
        Order actualBuyOrder = buyOrder.getSide() == OrderSide.BUY ? buyOrder : sellOrder;
        Order actualSellOrder = buyOrder.getSide() == OrderSide.SELL ? buyOrder : sellOrder;
        
        // Buy price must be >= sell price for match
        return actualBuyOrder.getPrice().compareTo(actualSellOrder.getPrice()) >= 0;
    }
    
    private Trade createTrade(Order buyOrder, Order sellOrder, BigDecimal price, BigDecimal quantity) {
        Order actualBuyOrder = buyOrder.getSide() == OrderSide.BUY ? buyOrder : sellOrder;
        Order actualSellOrder = buyOrder.getSide() == OrderSide.SELL ? buyOrder : sellOrder;
        
        Trade trade = new Trade();
        // Generate unique trade ID using nanoTime and thread ID for uniqueness
        trade.setTradeId(System.nanoTime() + Thread.currentThread().getId());
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
    
    private void updateOrderAfterTrade(Order order, BigDecimal tradeQuantity, BigDecimal tradePrice) {
        BigDecimal newFilledQuantity = order.getFilledQuantity().add(tradeQuantity);
        order.setFilledQuantity(newFilledQuantity);
        
        // Update average price (weighted average)
        if (order.getAveragePrice() == null || order.getAveragePrice().compareTo(BigDecimal.ZERO) == 0) {
            order.setAveragePrice(tradePrice);
        } else {
            BigDecimal totalValue = order.getAveragePrice().multiply(order.getFilledQuantity().subtract(tradeQuantity))
                                   .add(tradePrice.multiply(tradeQuantity));
            order.setAveragePrice(totalValue.divide(newFilledQuantity, 4, java.math.RoundingMode.HALF_UP));
        }
        
        order.setUpdatedAt(LocalDateTime.now());
    }

    // PORTFOLIO SERVICE INTEGRATION HELPER METHODS

    /**
     * Performs portfolio trade confirmation for both buy and sell sides of a trade.
     * This method handles both full and partial trade confirmations.
     * @param trade The executed trade
     * @param newOrder The new order that triggered the trade
     * @param oppositeOrder The existing order that matched
     * @param executedQuantity The quantity that was executed in this trade
     * @return true if portfolio confirmation successful, false otherwise
     */
    private boolean performPortfolioTradeConfirmation(Trade trade, Order newOrder, Order oppositeOrder, 
                                                     BigDecimal executedQuantity) {
        try {
            // Determine buy and sell orders
            Order buyOrder = newOrder.getSide() == OrderSide.BUY ? newOrder : oppositeOrder;
            Order sellOrder = newOrder.getSide() == OrderSide.SELL ? newOrder : oppositeOrder;
            
            // Calculate remaining quantities after this trade
            BigDecimal buyOrderRemainingAfterTrade = buyOrder.getRemainingQuantity().subtract(executedQuantity);
            BigDecimal sellOrderRemainingAfterTrade = sellOrder.getRemainingQuantity().subtract(executedQuantity);
            
            // Determine confirmation type (full vs partial)
            boolean isBuyOrderPartial = buyOrderRemainingAfterTrade.compareTo(BigDecimal.ZERO) > 0;
            boolean isSellOrderPartial = sellOrderRemainingAfterTrade.compareTo(BigDecimal.ZERO) > 0;
            
            CompletableFuture<PortfolioResponse> buyConfirmationFuture = null;
            CompletableFuture<PortfolioResponse> sellConfirmationFuture = null;
            
            // Only confirm portfolio operations for real users (non-bot orders)
            boolean shouldConfirmBuyOrder = !buyOrder.getIsBot();
            boolean shouldConfirmSellOrder = !sellOrder.getIsBot();
            
            log.info("🤖 BOT CHECK: BuyOrder isBot={}, SellOrder isBot={} → Confirming: Buy={}, Sell={}", 
                    buyOrder.getIsBot(), sellOrder.getIsBot(), shouldConfirmBuyOrder, shouldConfirmSellOrder);
            
            if (shouldConfirmBuyOrder && isBuyOrderPartial) {
                // Partial buy trade confirmation
                PartialTradeConfirmationRequest buyRequest = PartialTradeConfirmationRequest.builder()
                        .tradeId(trade.getTradeId())
                        .orderId(buyOrder.getOrderId())
                        .accountId(buyOrder.getAccountId())
                        .symbol(buyOrder.getSymbol())
                        .partialQuantity(executedQuantity.intValue())
                        .remainingQuantity(buyOrderRemainingAfterTrade.intValue())
                        .executedPrice(trade.getPrice())
                        .build();
                
                log.info("🏦 PORTFOLIO: Confirming PARTIAL BUY trade → {}", buyRequest);
                buyConfirmationFuture = portfolioClient.confirmPartialBuyTrade(buyRequest);
            } else if (shouldConfirmBuyOrder) {
                // Full buy trade confirmation
                TradeConfirmationRequest buyRequest = TradeConfirmationRequest.builder()
                        .tradeId(trade.getTradeId())
                        .orderId(buyOrder.getOrderId())
                        .accountId(buyOrder.getAccountId())
                        .symbol(buyOrder.getSymbol())
                        .executedQuantity(executedQuantity.intValue())
                        .executedPrice(trade.getPrice())
                        .build();
                
                log.info("🏦 PORTFOLIO: Confirming FULL BUY trade → {}", buyRequest);
                buyConfirmationFuture = portfolioClient.confirmBuyTrade(buyRequest);
            } else {
                log.info("🤖 PORTFOLIO: Skipping BUY confirmation - Order is from BOT → OrderId: {}", buyOrder.getOrderId());
            }
            
            if (shouldConfirmSellOrder && isSellOrderPartial) {
                // Partial sell trade confirmation
                PartialTradeConfirmationRequest sellRequest = PartialTradeConfirmationRequest.builder()
                        .tradeId(trade.getTradeId())
                        .orderId(sellOrder.getOrderId())
                        .accountId(sellOrder.getAccountId())
                        .symbol(sellOrder.getSymbol())
                        .partialQuantity(executedQuantity.intValue())
                        .remainingQuantity(sellOrderRemainingAfterTrade.intValue())
                        .executedPrice(trade.getPrice())
                        .build();
                
                log.info("🏦 PORTFOLIO: Confirming PARTIAL SELL trade → {}", sellRequest);
                sellConfirmationFuture = portfolioClient.confirmPartialSellTrade(sellRequest);
            } else if (shouldConfirmSellOrder) {
                // Full sell trade confirmation
                TradeConfirmationRequest sellRequest = TradeConfirmationRequest.builder()
                        .tradeId(trade.getTradeId())
                        .orderId(sellOrder.getOrderId())
                        .accountId(sellOrder.getAccountId())
                        .symbol(sellOrder.getSymbol())
                        .executedQuantity(executedQuantity.intValue())
                        .executedPrice(trade.getPrice())
                        .build();
                
                log.info("🏦 PORTFOLIO: Confirming FULL SELL trade → {}", sellRequest);
                sellConfirmationFuture = portfolioClient.confirmSellTrade(sellRequest);
            } else {
                log.info("🤖 PORTFOLIO: Skipping SELL confirmation - Order is from BOT → OrderId: {}", sellOrder.getOrderId());
            }
            
            // Wait for confirmations to complete (only for real users)
            PortfolioResponse buyResponse = null;
            PortfolioResponse sellResponse = null;
            
            if (buyConfirmationFuture != null) {
                buyResponse = buyConfirmationFuture.get();
            }
            if (sellConfirmationFuture != null) {
                sellResponse = sellConfirmationFuture.get();
            }
            
            // Check responses (consider success if no confirmation was needed due to bot orders)
            boolean buySuccess = (buyResponse == null) || buyResponse.isSuccess(); // null means bot order (no confirmation needed)
            boolean sellSuccess = (sellResponse == null) || sellResponse.isSuccess(); // null means bot order (no confirmation needed)
            
            if (buySuccess && sellSuccess) {
                log.info("🏦 PORTFOLIO: Trade confirmations successful → TradeId: {}, BuySuccess: {}, SellSuccess: {}", 
                        trade.getTradeId(), buySuccess, sellSuccess);
                return true;
            } else {
                log.error("🏦 PORTFOLIO: Trade confirmation failed → TradeId: {}, BuySuccess: {}, SellSuccess: {}", 
                        trade.getTradeId(), buySuccess, sellSuccess);
                
                if (buyResponse != null && !buyResponse.isSuccess()) {
                    log.error("🏦 PORTFOLIO: Buy confirmation error → {}", buyResponse.getMessage());
                }
                if (sellResponse != null && !sellResponse.isSuccess()) {
                    log.error("🏦 PORTFOLIO: Sell confirmation error → {}", sellResponse.getMessage());
                }
                
                // TODO: In Phase 3, implement compensation mechanism for failed confirmations
                return false;
            }
            
        } catch (ExecutionException e) {
            log.error("🏦 PORTFOLIO: Trade confirmation execution error → TradeId: {}", trade.getTradeId(), e);
            
            // Check if the cause is a known portfolio exception
            if (e.getCause() instanceof PortfolioServiceException portfolioEx) {
                log.error("🏦 PORTFOLIO: Portfolio service error during trade confirmation → ErrorCode: {}, Message: {}", 
                        portfolioEx.getErrorCode(), portfolioEx.getMessage());
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("🏦 PORTFOLIO: Trade confirmation interrupted → TradeId: {}", trade.getTradeId(), e);
            return false;
        } catch (Exception e) {
            log.error("🏦 PORTFOLIO: Unexpected error during trade confirmation → TradeId: {}", trade.getTradeId(), e);
            return false;
        }
    }
    
    /**
     * AUTO-CONFIRM: Automatically confirm filled order in Portfolio Service
     * This method is called when an order is marked as FILLED to automatically
     * call the appropriate Portfolio Service confirm endpoint.
     */
    private void autoConfirmFilledOrder(Order filledOrder, Trade trade) {
        try {
            log.info("🤖 AUTO-CONFIRM: Processing filled order → OrderId: {}, Side: {}, Quantity: {}", 
                    filledOrder.getOrderId(), filledOrder.getSide(), filledOrder.getQuantity());
            
            // Skip auto-confirm for bot orders (account ID contains "BOT")
            String accountIdStr = String.valueOf(filledOrder.getAccountId());
            if (accountIdStr.contains("BOT")) {
                log.info("🤖 AUTO-CONFIRM: Skipping auto-confirm for BOT order → OrderId: {}", filledOrder.getOrderId());
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
                    
                    log.info("🤖 AUTO-CONFIRM (ORDER BOOK): Confirming FULLY FILLED BUY order → {}", buyRequest);
                    confirmationFuture = portfolioClient.confirmBuyTrade(buyRequest);
                } else if (filledOrder.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                    // PARTIALLY FILLED - Use confirmPartialBuyTrade
                    PartialTradeConfirmationRequest partialBuyRequest = PartialTradeConfirmationRequest.builder()
                            .tradeId(trade.getTradeId())
                            .orderId(filledOrder.getOrderId())
                            .partialQuantity(trade.getQuantity().intValue())  // Actually filled quantity in this trade
                            .remainingQuantity(filledOrder.getRemainingQuantity().intValue())
                            .executedPrice(trade.getPrice())
                            .build();
                    
                    log.info("🤖 AUTO-CONFIRM (ORDER BOOK): Confirming PARTIALLY FILLED BUY order → {}", partialBuyRequest);
                    confirmationFuture = portfolioClient.confirmPartialBuyTrade(partialBuyRequest);
                } else {
                    log.warn("🤖 AUTO-CONFIRM (ORDER BOOK): Unexpected order status for BUY order → OrderId: {}, Status: {}", 
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
                    
                    log.info("🤖 AUTO-CONFIRM (ORDER BOOK): Confirming FULLY FILLED SELL order → {}", sellRequest);
                    confirmationFuture = portfolioClient.confirmSellTrade(sellRequest);
                } else if (filledOrder.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                    // PARTIALLY FILLED - Use confirmPartialSellTrade
                    PartialTradeConfirmationRequest partialSellRequest = PartialTradeConfirmationRequest.builder()
                            .tradeId(trade.getTradeId())
                            .orderId(filledOrder.getOrderId())
                            .partialQuantity(trade.getQuantity().intValue())  // Actually filled quantity in this trade
                            .remainingQuantity(filledOrder.getRemainingQuantity().intValue())
                            .executedPrice(trade.getPrice())
                            .build();
                    
                    log.info("🤖 AUTO-CONFIRM (ORDER BOOK): Confirming PARTIALLY FILLED SELL order → {}", partialSellRequest);
                    confirmationFuture = portfolioClient.confirmPartialSellTrade(partialSellRequest);
                } else {
                    log.warn("🤖 AUTO-CONFIRM (ORDER BOOK): Unexpected order status for SELL order → OrderId: {}, Status: {}", 
                            filledOrder.getOrderId(), filledOrder.getStatus());
                    return;
                }
            }
            
            // Execute confirmation asynchronously without blocking
            confirmationFuture.thenAccept(response -> {
                if (response.isSuccess()) {
                    log.info("✅ AUTO-CONFIRM: Order confirmation successful → OrderId: {}", filledOrder.getOrderId());
                } else {
                    log.error("❌ AUTO-CONFIRM: Order confirmation failed → OrderId: {}, Error: {}", 
                            filledOrder.getOrderId(), response.getMessage());
                }
            }).exceptionally(throwable -> {
                log.error("🚨 AUTO-CONFIRM: Exception during order confirmation → OrderId: {}, Error: {}", 
                        filledOrder.getOrderId(), throwable.getMessage());
                return null;
            });
            
        } catch (Exception e) {
            log.error("🚨 AUTO-CONFIRM: Unexpected error during auto-confirm → OrderId: {}, Error: {}", 
                    filledOrder.getOrderId(), e.getMessage());
        }
    }
}