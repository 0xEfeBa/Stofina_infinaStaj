package com.stofina.app.marketdataservice.kafka;

 import com.stofina.app.marketdataservice.dto.response.StockResponse;
 import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MarketDataProducer {
    private final KafkaTemplate<String, StockResponse> kafkaTemplate;

    public MarketDataProducer(KafkaTemplate<String, StockResponse> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendStockUpdate(StockResponse stockResponse) {
        kafkaTemplate.send("stock-topic", stockResponse);
        System.out.println("Stock message sent to Kafka: " + stockResponse.getSymbol());
    }
}
