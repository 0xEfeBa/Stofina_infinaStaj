package com.stofina.app.orderservice.kafka;

import com.stofina.app.orderservice.dto.response.StockResponse;
import com.stofina.app.orderservice.service.IStopLossService;
import com.stofina.app.orderservice.service.KafkaPriceCache;
import com.stofina.app.orderservice.service.SimpleOrderBookManager;
import com.stofina.app.orderservice.service.PendingOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderConsumer {

    @Autowired
    private IStopLossService stopLossService;

    @Autowired
    private SimpleOrderBookManager orderBookManager;

    @Autowired
    private KafkaPriceCache kafkaPriceCache;
    
    @Autowired
    private PendingOrderService pendingOrderService;

    @KafkaListener(topics = "stock-topic", groupId = "order-service-group")
    public void consumeStockMessage(StockResponse stockResponse) {
        log.info("Kafka'dan gelen hisse güncellemesi: symbol={}, price={}, change={}%",
                stockResponse.getSymbol(), stockResponse.getCurrentPrice(), stockResponse.getChangePercent());
        
        try {
            // Kafka'dan gelen fiyatı cache'le (ana amaç!)
            kafkaPriceCache.updatePrice(stockResponse.getSymbol(), stockResponse.getCurrentPrice());
            
            // Stop-Loss kontrollerini yap (mevcut method'u kullan)
            var stopLossResult = stopLossService.checkPrice(stockResponse.getSymbol(), stockResponse.getCurrentPrice());
            if (stopLossResult.isSuccess() && !stopLossResult.getData().isEmpty()) {
                log.info("Stop-loss tetiklendi - symbol: {}, triggered orders: {}", 
                        stockResponse.getSymbol(), stopLossResult.getData().size());
            }
            
            // 🆕 PENDING ORDER CHECK: Bekleyen emirleri kontrol et ve aktive et
            try {
                var activatedOrders = pendingOrderService.checkAndActivatePendingOrders(
                        stockResponse.getSymbol(), stockResponse.getCurrentPrice());
                
                if (!activatedOrders.isEmpty()) {
                    log.info("🟢 KAFKA: {} bekleyen emir aktive edildi - symbol: {}, price: {}", 
                            activatedOrders.size(), stockResponse.getSymbol(), stockResponse.getCurrentPrice());
                    
                    for (var activatedOrder : activatedOrders) {
                        log.info("🟢 KAFKA: Aktive edilen emir - OrderId: {}, Symbol: {}, OrderPrice: {}, MarketPrice: {}", 
                                activatedOrder.getOrderId(), activatedOrder.getSymbol(), 
                                activatedOrder.getPrice(), stockResponse.getCurrentPrice());
                    }
                } else {
                    log.debug("🟡 KAFKA: Bu fiyat güncellemesinde aktive edilen emir yok - symbol: {}, price: {}", 
                            stockResponse.getSymbol(), stockResponse.getCurrentPrice());
                }
            } catch (Exception pendingEx) {
                log.error("❌ KAFKA: Pending order kontrolü sırasında hata - symbol: {}, error: {}", 
                         stockResponse.getSymbol(), pendingEx.getMessage(), pendingEx);
            }
            
            log.debug("Symbol {} için Kafka fiyat güncellemesi işlendi ve cache'lendi", stockResponse.getSymbol());
            
        } catch (Exception e) {
            log.error("Kafka mesaj işleme hatası - symbol: {}, error: {}", 
                     stockResponse.getSymbol(), e.getMessage(), e);
        }
    }
}
