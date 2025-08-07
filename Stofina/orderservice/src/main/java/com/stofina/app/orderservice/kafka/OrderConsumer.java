package com.stofina.app.orderservice.kafka;

import com.stofina.app.orderservice.dto.response.StockResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderConsumer {

    @KafkaListener(topics = "stock-topic", groupId = "order-service-group")
    public void consumeStockMessage(StockResponse stockResponse) {
        log.info("Kafka’dan gelen hisse güncellemesi: symbol={}, price={}",
                stockResponse.getSymbol(), stockResponse.getCurrentPrice());
    }
}