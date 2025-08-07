package com.stofina.app.orderservice.controller;

import com.stofina.app.orderservice.service.client.MarketDataClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/market-data")
@RequiredArgsConstructor
public class MarketDataTestController {

    private final MarketDataClient marketDataClient;

    @GetMapping("/{symbol}")
    public ResponseEntity<Map<String, Object>> getPrice(@PathVariable String symbol) {
        try {
            BigDecimal price = marketDataClient.getCurrentPrice(symbol);
            
            Map<String, Object> response = new HashMap<>();
            response.put("symbol", symbol);
            response.put("price", price);
            response.put("source", "MarketDataService");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get price for " + symbol);
            error.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
}