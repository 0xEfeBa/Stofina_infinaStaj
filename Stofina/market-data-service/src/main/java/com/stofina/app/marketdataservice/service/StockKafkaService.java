package com.stofina.app.marketdataservice.service;

import com.stofina.app.marketdataservice.dto.response.StockResponse;
import com.stofina.app.marketdataservice.kafka.MarketDataProducer;
import com.stofina.app.marketdataservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class StockKafkaService {

    private final StockRepository stockRepository;
    private final MarketDataProducer producer;

    public void sendAllStocksToKafka() {
        stockRepository.findAll().forEach(stock -> {
            BigDecimal changeAmount = stock.getCurrentPrice().subtract(stock.getDefaultPrice());

            BigDecimal changePercent = changeAmount
                    .divide(stock.getDefaultPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            StockResponse response = new StockResponse(
                    stock.getSymbol(),
                    stock.getStockName(),
                    stock.getCompanyName(),
                    stock.getEquityMarket(),
                    stock.getExchange(),
                    stock.getCurrency(),
                    stock.getIsinCode(),
                    stock.getCurrentPrice(),
                    stock.getDefaultPrice(),
                    changeAmount,
                    changePercent,
                    stock.getStatus(),
                    stock.getLastUpdated()
            );

            producer.sendStockUpdate(response);
        });
    }

}