package com.stofina.orderservice.service.impl;

import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.model.SimpleOrderBook;
import com.stofina.orderservice.model.SimpleOrderBookSnapshot;
import com.stofina.orderservice.service.SimpleOrderBookManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class SimpleOrderBookManagerImpl implements SimpleOrderBookManager {
    
    // CHECKPOINT 5.2 - Thread-Safe Order Book Management
    private final ConcurrentHashMap<String, SimpleOrderBook> orderBooks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> symbolLocks = new ConcurrentHashMap<>();
    
    // TODO: ENTEGRASYON SIRASINDA KALDIRILACAK - Mock BIST symbols for testing
    private static final List<String> MOCK_BIST_SYMBOLS = Arrays.asList(
        "THYAO", "GARAN", "AKBNK", "ISCTR", "TUPRS", 
        "ASELS", "SISE", "BIMAS", "KCHOL", "TCELL"
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
    public boolean addOrder(Order order) {
        if (order == null || order.getSymbol() == null) {
            return false;
        }
        
        String symbol = order.getSymbol().trim().toUpperCase();
        ReentrantLock lock = acquireLock(symbol);
        
        try {
            SimpleOrderBook orderBook = getOrCreateOrderBook(symbol);
            orderBook.addOrder(order);
            log.debug("Order added to book: {} for symbol: {}", order.getOrderId(), symbol);
            return true;
        } catch (Exception e) {
            log.error("Failed to add order {} for symbol {}: {}", order.getOrderId(), symbol, e.getMessage());
            return false;
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
    
    private int calculateTotalQuantity(java.util.List<com.stofina.orderservice.model.OrderLevel> levels) {
        return levels != null ? 
            levels.stream().mapToInt(level -> level.getQuantity().intValue()).sum() : 0;
    }
}