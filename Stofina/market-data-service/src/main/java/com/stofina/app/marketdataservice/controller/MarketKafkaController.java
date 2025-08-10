package com.stofina.app.marketdataservice.controller;

import com.stofina.app.marketdataservice.dto.response.StockResponse;
import com.stofina.app.marketdataservice.kafka.MarketDataProducer;
import com.stofina.app.marketdataservice.service.StockKafkaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/market")
public class MarketKafkaController {
    private final MarketDataProducer producer;
    private final StockKafkaService stockKafkaService;

    public MarketKafkaController(MarketDataProducer producer, StockKafkaService stockKafkaService) {
        this.producer = producer;
        this.stockKafkaService = stockKafkaService;
    }

    @PostMapping("/send-stock")
    public ResponseEntity<String> sendPrice(@RequestBody StockResponse stockResponse) {
        producer.sendStockUpdate(stockResponse);
        return ResponseEntity.ok("Stock sent to Kafka");
    }

    @GetMapping("/send-all")
    public ResponseEntity<String> sendAllStocks() {
        stockKafkaService.sendAllStocksToKafka();
        return ResponseEntity.ok("Tüm hisseler Kafka’ya gönderildi");
    }
}
