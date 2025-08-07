package com.stofina.orderservice.service.impl;

import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.enums.OrderSide;
import com.stofina.orderservice.model.DisplayOrder;
import com.stofina.orderservice.model.OrderLevel;
import com.stofina.orderservice.model.SimpleOrderBookSnapshot;
import com.stofina.orderservice.service.DisplayOrderBookService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DisplayOrderBookServiceImpl implements DisplayOrderBookService {
    
    // CHECKPOINT 4.1 - Display-Only Order Book Implementation
    private final Map<String, List<DisplayOrder>> displayBids = new ConcurrentHashMap<>();
    private final Map<String, List<DisplayOrder>> displayAsks = new ConcurrentHashMap<>();
    private final Map<String, List<DisplayOrder>> userOrders = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    // TODO: ENTEGRASYON SIRASINDA KALDIRILACAK - Mock BIST symbols
    private static final List<String> MOCK_BIST_SYMBOLS = Arrays.asList(
        "THYAO", "GARAN", "AKBNK", "ISCTR", "TUPRS", 
        "ASELS", "SISE", "BIMAS", "KCHOL", "TCELL"
    );
    
    // TODO: ENTEGRASYON SIRASINDA KALDIRILACAK - Mock initial prices
    private static final Map<String, BigDecimal> MOCK_INITIAL_PRICES = Map.of(
        "THYAO", new BigDecimal("45.50"),
        "GARAN", new BigDecimal("28.75"),
        "AKBNK", new BigDecimal("42.30"),
        "ISCTR", new BigDecimal("15.80"),
        "TUPRS", new BigDecimal("38.90"),
        "ASELS", new BigDecimal("35.20"),
        "SISE", new BigDecimal("22.40"),
        "BIMAS", new BigDecimal("95.50"),
        "KCHOL", new BigDecimal("18.60"),
        "TCELL", new BigDecimal("52.80")
    );
    
    @PostConstruct
    public void initializeAllDisplayBooks() {
        log.info("Initializing display order books for all symbols...");
        MOCK_BIST_SYMBOLS.forEach(symbol -> {
            BigDecimal price = MOCK_INITIAL_PRICES.get(symbol);
            initializeDisplayOrderBook(symbol, price);
            log.debug("Display order book initialized for symbol: {}", symbol);
        });
        log.info("All display order books initialized: {} symbols", MOCK_BIST_SYMBOLS.size());
    }
    
    @Override
    public void initializeDisplayOrderBook(String symbol, BigDecimal currentPrice) {
        if (symbol == null || currentPrice == null) {
            log.warn("Cannot initialize display order book: symbol or price is null");
            return;
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        // Clear existing display orders
        displayBids.put(normalizedSymbol, new ArrayList<>());
        displayAsks.put(normalizedSymbol, new ArrayList<>());
        userOrders.put(normalizedSymbol, new ArrayList<>());
        
        // Create unique bot orders
        createUniqueBidOrders(normalizedSymbol, currentPrice);
        createUniqueAskOrders(normalizedSymbol, currentPrice);
        
        log.info("Display order book initialized for {}: {} BID, {} ASK orders", 
                normalizedSymbol, displayBids.get(normalizedSymbol).size(), 
                displayAsks.get(normalizedSymbol).size());
    }
    
    @Override
    public void updateDisplayPrices(String symbol, BigDecimal newPrice) {
        if (symbol == null || newPrice == null) {
            return;
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        // Clear and recreate bot orders with new price
        clearBotOrders(normalizedSymbol);
        createUniqueBidOrders(normalizedSymbol, newPrice);
        createUniqueAskOrders(normalizedSymbol, newPrice);
        
        log.debug("Display prices updated for {}: new center price {}", normalizedSymbol, newPrice);
    }
    
    @Override
    public SimpleOrderBookSnapshot getDisplaySnapshot(String symbol) {
        if (symbol == null) {
            return null;
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        // Combine bot orders and user orders
        List<DisplayOrder> allBids = combineOrders(normalizedSymbol, OrderSide.BUY);
        List<DisplayOrder> allAsks = combineOrders(normalizedSymbol, OrderSide.SELL);
        
        // Convert to OrderLevel format and get top 10
        List<OrderLevel> bidLevels = convertToOrderLevels(allBids, true);
        List<OrderLevel> askLevels = convertToOrderLevels(allAsks, false);
        
        // Calculate best prices and spread
        BigDecimal bestBid = bidLevels.isEmpty() ? null : bidLevels.get(0).getPrice();
        BigDecimal bestAsk = askLevels.isEmpty() ? null : askLevels.get(0).getPrice();
        BigDecimal spread = calculateSpread(bestBid, bestAsk);
        
        return new SimpleOrderBookSnapshot(
            normalizedSymbol,
            bidLevels,
            askLevels,
            bestBid,
            bestAsk,
            spread,
            LocalDateTime.now(),
            calculateTotalQuantity(bidLevels),
            calculateTotalQuantity(askLevels)
        );
    }
    
    @Override
    public void addUserOrderToDisplay(Order userOrder) {
        if (userOrder == null || userOrder.getSymbol() == null) {
            return;
        }
        
        String symbol = userOrder.getSymbol().trim().toUpperCase();
        
        DisplayOrder displayOrder = new DisplayOrder(
            symbol,
            userOrder.getSide(),
            userOrder.getPrice(),
            userOrder.getRemainingQuantity(),
            false // User order, not bot
        );
        
        userOrders.computeIfAbsent(symbol, k -> new ArrayList<>()).add(displayOrder);
        log.debug("User order added to display: {} {} @ {} × {}", 
                userOrder.getSide(), symbol, userOrder.getPrice(), userOrder.getRemainingQuantity());
    }
    
    @Override
    public void removeFromDisplay(String symbol, Long orderId) {
        if (symbol == null || orderId == null) {
            return;
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        List<DisplayOrder> userOrderList = userOrders.get(normalizedSymbol);
        
        if (userOrderList != null) {
            userOrderList.removeIf(order -> orderId.equals(order.getDisplayOrderId()));
        }
    }
    
    @Override
    public Set<String> getActiveDisplaySymbols() {
        return new HashSet<>(MOCK_BIST_SYMBOLS);
    }
    
    @Override
    public int getDisplayOrderCount(String symbol) {
        if (symbol == null) {
            return 0;
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        int botCount = displayBids.getOrDefault(normalizedSymbol, new ArrayList<>()).size() +
                      displayAsks.getOrDefault(normalizedSymbol, new ArrayList<>()).size();
        int userCount = userOrders.getOrDefault(normalizedSymbol, new ArrayList<>()).size();
        
        return botCount + userCount;
    }
    
    @Override
    public BigDecimal getDisplayBestBid(String symbol) {
        List<DisplayOrder> allBids = combineOrders(symbol, OrderSide.BUY);
        return allBids.stream()
                .map(DisplayOrder::getPrice)
                .max(BigDecimal::compareTo)
                .orElse(null);
    }
    
    @Override
    public BigDecimal getDisplayBestAsk(String symbol) {
        List<DisplayOrder> allAsks = combineOrders(symbol, OrderSide.SELL);
        return allAsks.stream()
                .map(DisplayOrder::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }
    
    @Override
    public void clearDisplayOrderBook(String symbol) {
        if (symbol == null) {
            return;
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        displayBids.remove(normalizedSymbol);
        displayAsks.remove(normalizedSymbol);
        userOrders.remove(normalizedSymbol);
        
        log.debug("Display order book cleared for symbol: {}", normalizedSymbol);
    }
    
    @Override
    public void maintainDisplayDepth(String symbol, BigDecimal currentPrice) {
        if (symbol == null || currentPrice == null) {
            return;
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        List<DisplayOrder> bids = displayBids.get(normalizedSymbol);
        List<DisplayOrder> asks = displayAsks.get(normalizedSymbol);
        
        // If less than 15 orders on either side, refill
        if (bids == null || bids.size() < 15) {
            addMoreBidOrders(normalizedSymbol, currentPrice, 25 - (bids != null ? bids.size() : 0));
        }
        
        if (asks == null || asks.size() < 15) {
            addMoreAskOrders(normalizedSymbol, currentPrice, 25 - (asks != null ? asks.size() : 0));
        }
    }
    
    private void createUniqueBidOrders(String symbol, BigDecimal currentPrice) {
        List<DisplayOrder> bids = new ArrayList<>();
        
        // TIER 1: ±0.5% (15 orders with micro-spreads)
        for (int i = 1; i <= 15; i++) {
            BigDecimal baseOffset = currentPrice.multiply(new BigDecimal("0.005")); // 0.5%
            BigDecimal microSpread = new BigDecimal("0.001").multiply(BigDecimal.valueOf(i));
            BigDecimal price = currentPrice.subtract(baseOffset).subtract(microSpread);
            price = price.setScale(3, RoundingMode.HALF_UP); // 3 decimal precision for uniqueness
            
            BigDecimal quantity = BigDecimal.valueOf(random.nextInt(500, 2001));
            bids.add(new DisplayOrder(symbol, OrderSide.BUY, price, quantity, true));
        }
        
        // TIER 2: 0.5%-1% (5 orders)
        for (int i = 1; i <= 5; i++) {
            BigDecimal baseOffset = currentPrice.multiply(new BigDecimal("0.01")); // 1%
            BigDecimal spacing = new BigDecimal("0.002").multiply(BigDecimal.valueOf(i));
            BigDecimal price = currentPrice.subtract(baseOffset).subtract(spacing);
            price = price.setScale(3, RoundingMode.HALF_UP);
            
            BigDecimal quantity = BigDecimal.valueOf(random.nextInt(200, 801));
            bids.add(new DisplayOrder(symbol, OrderSide.BUY, price, quantity, true));
        }
        
        // TIER 3: 1%-3% (5 orders)
        for (int i = 1; i <= 5; i++) {
            BigDecimal baseOffset = currentPrice.multiply(new BigDecimal("0.03")); // 3%
            BigDecimal spacing = new BigDecimal("0.004").multiply(BigDecimal.valueOf(i));
            BigDecimal price = currentPrice.subtract(baseOffset).add(spacing);
            price = price.setScale(3, RoundingMode.HALF_UP);
            
            BigDecimal quantity = BigDecimal.valueOf(random.nextInt(1000, 5001));
            bids.add(new DisplayOrder(symbol, OrderSide.BUY, price, quantity, true));
        }
        
        displayBids.put(symbol, bids);
    }
    
    private void createUniqueAskOrders(String symbol, BigDecimal currentPrice) {
        List<DisplayOrder> asks = new ArrayList<>();
        
        // TIER 1: ±0.5% (15 orders with micro-spreads)
        for (int i = 1; i <= 15; i++) {
            BigDecimal baseOffset = currentPrice.multiply(new BigDecimal("0.005")); // 0.5%
            BigDecimal microSpread = new BigDecimal("0.001").multiply(BigDecimal.valueOf(i));
            BigDecimal price = currentPrice.add(baseOffset).add(microSpread);
            price = price.setScale(3, RoundingMode.HALF_UP);
            
            BigDecimal quantity = BigDecimal.valueOf(random.nextInt(500, 2001));
            asks.add(new DisplayOrder(symbol, OrderSide.SELL, price, quantity, true));
        }
        
        // TIER 2: 0.5%-1% (5 orders)
        for (int i = 1; i <= 5; i++) {
            BigDecimal baseOffset = currentPrice.multiply(new BigDecimal("0.01")); // 1%
            BigDecimal spacing = new BigDecimal("0.002").multiply(BigDecimal.valueOf(i));
            BigDecimal price = currentPrice.add(baseOffset).add(spacing);
            price = price.setScale(3, RoundingMode.HALF_UP);
            
            BigDecimal quantity = BigDecimal.valueOf(random.nextInt(200, 801));
            asks.add(new DisplayOrder(symbol, OrderSide.SELL, price, quantity, true));
        }
        
        // TIER 3: 1%-3% (5 orders)
        for (int i = 1; i <= 5; i++) {
            BigDecimal baseOffset = currentPrice.multiply(new BigDecimal("0.03")); // 3%
            BigDecimal spacing = new BigDecimal("0.004").multiply(BigDecimal.valueOf(i));
            BigDecimal price = currentPrice.add(baseOffset).subtract(spacing);
            price = price.setScale(3, RoundingMode.HALF_UP);
            
            BigDecimal quantity = BigDecimal.valueOf(random.nextInt(1000, 5001));
            asks.add(new DisplayOrder(symbol, OrderSide.SELL, price, quantity, true));
        }
        
        displayAsks.put(symbol, asks);
    }
    
    private void clearBotOrders(String symbol) {
        displayBids.put(symbol, new ArrayList<>());
        displayAsks.put(symbol, new ArrayList<>());
    }
    
    private List<DisplayOrder> combineOrders(String symbol, OrderSide side) {
        if (symbol == null) {
            return new ArrayList<>();
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        List<DisplayOrder> botOrders = side == OrderSide.BUY ? 
            displayBids.getOrDefault(normalizedSymbol, new ArrayList<>()) :
            displayAsks.getOrDefault(normalizedSymbol, new ArrayList<>());
        
        List<DisplayOrder> userOrderList = userOrders.getOrDefault(normalizedSymbol, new ArrayList<>())
            .stream()
            .filter(order -> order.getSide() == side)
            .collect(Collectors.toList());
        
        List<DisplayOrder> combined = new ArrayList<>(botOrders);
        combined.addAll(userOrderList);
        
        return combined;
    }
    
    private List<OrderLevel> convertToOrderLevels(List<DisplayOrder> orders, boolean descending) {
        Map<BigDecimal, BigDecimal> priceLevels = new HashMap<>();
        
        // Group by price and sum quantities
        for (DisplayOrder order : orders) {
            priceLevels.merge(order.getPrice(), order.getQuantity(), BigDecimal::add);
        }
        
        // Convert to OrderLevel and sort
        List<OrderLevel> levels = priceLevels.entrySet().stream()
            .map(entry -> new OrderLevel(entry.getKey(), entry.getValue(), 1))
            .sorted(descending ? 
                Comparator.comparing(OrderLevel::getPrice).reversed() :
                Comparator.comparing(OrderLevel::getPrice))
            .limit(10) // Top 10 levels
            .collect(Collectors.toList());
        
        return levels;
    }
    
    private BigDecimal calculateSpread(BigDecimal bestBid, BigDecimal bestAsk) {
        if (bestBid == null || bestAsk == null) {
            return null;
        }
        return bestAsk.subtract(bestBid);
    }
    
    private int calculateTotalQuantity(List<OrderLevel> levels) {
        return levels.stream().mapToInt(level -> level.getQuantity().intValue()).sum();
    }
    
    private void addMoreBidOrders(String symbol, BigDecimal currentPrice, int count) {
        List<DisplayOrder> existingBids = displayBids.get(symbol);
        if (existingBids == null) {
            existingBids = new ArrayList<>();
            displayBids.put(symbol, existingBids);
        }
        
        for (int i = 0; i < count; i++) {
            BigDecimal price = currentPrice.subtract(
                currentPrice.multiply(new BigDecimal("0.01").multiply(BigDecimal.valueOf(i + 1)))
            ).setScale(3, RoundingMode.HALF_UP);
            
            BigDecimal quantity = BigDecimal.valueOf(random.nextInt(500, 2001));
            existingBids.add(new DisplayOrder(symbol, OrderSide.BUY, price, quantity, true));
        }
    }
    
    private void addMoreAskOrders(String symbol, BigDecimal currentPrice, int count) {
        List<DisplayOrder> existingAsks = displayAsks.get(symbol);
        if (existingAsks == null) {
            existingAsks = new ArrayList<>();
            displayAsks.put(symbol, existingAsks);
        }
        
        for (int i = 0; i < count; i++) {
            BigDecimal price = currentPrice.add(
                currentPrice.multiply(new BigDecimal("0.01").multiply(BigDecimal.valueOf(i + 1)))
            ).setScale(3, RoundingMode.HALF_UP);
            
            BigDecimal quantity = BigDecimal.valueOf(random.nextInt(500, 2001));
            existingAsks.add(new DisplayOrder(symbol, OrderSide.SELL, price, quantity, true));
        }
    }
    
    @Override
    public List<DisplayOrder> getUserOrdersForSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        List<DisplayOrder> userOrderList = userOrders.get(normalizedSymbol);
        
        if (userOrderList == null) {
            return new ArrayList<>();
        }
        
        // Return copy to avoid external modifications
        return new ArrayList<>(userOrderList);
    }
}