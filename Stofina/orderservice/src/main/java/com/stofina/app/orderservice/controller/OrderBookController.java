package com.stofina.app.orderservice.controller;

import com.stofina.app.orderservice.constants.ApiEndpoints;
import com.stofina.app.orderservice.constants.MockDataConstants;
import com.stofina.app.orderservice.dto.response.OrderBookResponse;
import com.stofina.app.orderservice.model.SimpleOrderBookSnapshot;
import com.stofina.app.orderservice.service.DisplayOrderBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping(ApiEndpoints.ORDER_BOOK_BASE)
@RequiredArgsConstructor
@Slf4j
public class OrderBookController {
    
    private final DisplayOrderBookService displayOrderBookService;
    
    // CHECKPOINT 5.3 - Order Book HTTP REST Endpoints
    
    @GetMapping(ApiEndpoints.GET_SYMBOLS)
    public ResponseEntity<Map<String, Object>> getActiveSymbols() {
        log.info("Fetching active symbols (mock data)");
        return ResponseEntity.ok(createSymbolsResponse());
    }
    
    private Map<String, Object> createSymbolsResponse() {
        Set<String> activeSymbols = displayOrderBookService.getActiveDisplaySymbols();
        
        Map<String, Object> response = new HashMap<>();
        response.put("symbols", activeSymbols);
        response.put("count", activeSymbols.size());
        response.put("type", MockDataConstants.MOCK_DATA_TYPE);
        response.put("timestamp", LocalDateTime.now());
        
        return response;
    }
    
    @GetMapping(ApiEndpoints.GET_ORDER_BOOK)
    public ResponseEntity<OrderBookResponse> getOrderBook(@PathVariable String symbol) {
        log.info("Fetching display order book for symbol: {}", symbol);
        
        if (symbol == null || symbol.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        // Check if symbol exists in display order book
        if (!displayOrderBookService.getActiveDisplaySymbols().contains(normalizedSymbol)) {
            log.warn("Symbol not found in display order book: {}", normalizedSymbol);
            return ResponseEntity.notFound().build();
        }
        
        // Get display snapshot with bot orders
        SimpleOrderBookSnapshot snapshot = displayOrderBookService.getDisplaySnapshot(normalizedSymbol);
        OrderBookResponse response = OrderBookResponse.fromSnapshot(snapshot);
        
        log.info("Display order book returned for {}: {} total orders", 
                normalizedSymbol, snapshot != null ? snapshot.getTotalOrderCount() : 0);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping(ApiEndpoints.GET_BEST_PRICES)
    public ResponseEntity<Map<String, Object>> getBestPrices(@PathVariable String symbol) {
        log.info("Fetching best prices for symbol: {}", symbol);
        
        if (symbol == null || symbol.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        if (!displayOrderBookService.getActiveDisplaySymbols().contains(normalizedSymbol)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(createBestPricesResponse(normalizedSymbol));
    }
    
    // CHECKPOINT B2 - Added snapshot and depth endpoints for 5v5 order book display
    
    @GetMapping(ApiEndpoints.GET_ORDER_BOOK_SNAPSHOT)
    public ResponseEntity<Map<String, Object>> getOrderBookSnapshot(@PathVariable String symbol) {
        log.info("Fetching order book snapshot (5v5) for symbol: {}", symbol);
        
        if (symbol == null || symbol.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        if (!displayOrderBookService.getActiveDisplaySymbols().contains(normalizedSymbol)) {
            log.warn("Symbol not found: {}", normalizedSymbol);
            return ResponseEntity.notFound().build();
        }
        
        // Get combined snapshot (user orders + bot orders)
        SimpleOrderBookSnapshot snapshot = displayOrderBookService.getDisplaySnapshot(normalizedSymbol);
        
        // Create 5v5 table response
        Map<String, Object> response = new HashMap<>();
        response.put("symbol", normalizedSymbol);
        response.put("bids", snapshot.getBids() != null ? snapshot.getBids().subList(0, Math.min(5, snapshot.getBids().size())) : new ArrayList<>());
        response.put("asks", snapshot.getAsks() != null ? snapshot.getAsks().subList(0, Math.min(5, snapshot.getAsks().size())) : new ArrayList<>());
        response.put("bestBid", snapshot.getBestBid());
        response.put("bestAsk", snapshot.getBestAsk()); 
        response.put("spread", snapshot.getSpread());
        response.put("timestamp", LocalDateTime.now());
        response.put("totalBidQuantity", snapshot.getTotalBidQuantity());
        response.put("totalAskQuantity", snapshot.getTotalAskQuantity());
        
        log.info("Order book snapshot returned for {}: {} bids, {} asks", 
                normalizedSymbol, 
                snapshot.getBids() != null ? Math.min(5, snapshot.getBids().size()) : 0,
                snapshot.getAsks() != null ? Math.min(5, snapshot.getAsks().size()) : 0);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping(ApiEndpoints.GET_ORDER_BOOK_DEPTH)
    public ResponseEntity<Map<String, Object>> getOrderBookDepth(@PathVariable String symbol) {
        log.info("Fetching order book depth for symbol: {}", symbol);
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        if (!displayOrderBookService.getActiveDisplaySymbols().contains(normalizedSymbol)) {
            return ResponseEntity.notFound().build();
        }
        
        SimpleOrderBookSnapshot snapshot = displayOrderBookService.getDisplaySnapshot(normalizedSymbol);
        
        Map<String, Object> response = new HashMap<>();
        response.put("symbol", normalizedSymbol);
        response.put("bids", snapshot.getBids()); // Full depth
        response.put("asks", snapshot.getAsks()); // Full depth
        response.put("bestBid", snapshot.getBestBid());
        response.put("bestAsk", snapshot.getBestAsk());
        response.put("spread", snapshot.getSpread());
        response.put("timestamp", LocalDateTime.now());
        response.put("totalBidQuantity", snapshot.getTotalBidQuantity());
        response.put("totalAskQuantity", snapshot.getTotalAskQuantity());
        
        return ResponseEntity.ok(response);
    }
    
    // CHECKPOINT B1 - Order creation endpoint removed
    // All order creation now goes through OrderController at POST /api/v1/orders
    
    @GetMapping(ApiEndpoints.GET_ORDER_BOOK_STATS)
    public ResponseEntity<Map<String, Object>> getOrderBookStats(@PathVariable String symbol) {
        log.info("Fetching display order book stats for symbol: {}", symbol);
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        if (!displayOrderBookService.getActiveDisplaySymbols().contains(normalizedSymbol)) {
            return ResponseEntity.notFound().build();
        }
        
        int totalOrders = displayOrderBookService.getDisplayOrderCount(normalizedSymbol);
        SimpleOrderBookSnapshot snapshot = displayOrderBookService.getDisplaySnapshot(normalizedSymbol);
        
        Map<String, Object> response = new HashMap<>();
        response.put("symbol", normalizedSymbol);
        response.put("totalOrders", totalOrders);
        response.put("bidLevels", snapshot.getBids() != null ? snapshot.getBids().size() : 0);
        response.put("askLevels", snapshot.getAsks() != null ? snapshot.getAsks().size() : 0);
        response.put("isEmpty", snapshot.isEmpty());
        response.put("hasSpread", snapshot.hasSpread());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    // CHECKPOINT B1 - Removed unused helper methods (convertToEntity, validateAndNormalizeSymbol, isSymbolSupported)
    
    private Map<String, Object> createBestPricesResponse(String symbol) {
        BigDecimal bestBid = displayOrderBookService.getDisplayBestBid(symbol);
        BigDecimal bestAsk = displayOrderBookService.getDisplayBestAsk(symbol);
        BigDecimal spread = calculateSpread(bestBid, bestAsk);
        
        Map<String, Object> response = new HashMap<>();
        response.put("symbol", symbol);
        response.put("bestBid", bestBid);
        response.put("bestAsk", bestAsk);
        response.put("spread", spread);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    private BigDecimal calculateSpread(BigDecimal bestBid, BigDecimal bestAsk) {
        return (bestBid != null && bestAsk != null) ? bestAsk.subtract(bestBid) : null;
    }
}