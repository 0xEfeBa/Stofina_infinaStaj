package com.stofina.app.marketdataservice.service.impl;

import com.stofina.app.marketdataservice.dto.response.StockResponse;
import com.stofina.app.marketdataservice.kafka.MarketDataProducer;
import com.stofina.app.marketdataservice.repository.StockRepository;
import com.stofina.app.marketdataservice.service.IStockKafkaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class StockKafkaServiceImpl implements IStockKafkaService {

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
                    stock.getCompanyName(),
                    stock.getCurrentPrice().doubleValue(),
                    stock.getDefaultPrice().doubleValue(),
                    changeAmount.doubleValue(),
                    changePercent.doubleValue(),
                    stock.getLastUpdated()
            );
            producer.sendStockUpdate(response);
        });
    }

}