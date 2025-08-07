package com.stofina.app.market_data_service.controller;

import com.stofina.market_data_service.dto.response.PriceResponse;
import com.stofina.market_data_service.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/market")
public class MarketDataController {

    private final StatisticsService statisticsService;

    @GetMapping("/prices")
    public ResponseEntity<List<PriceResponse>> getAllPrices(){
        List<PriceResponse> prices = statisticsService.getAllPrices();
        return ResponseEntity.ok(prices);
    }

}
