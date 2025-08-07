package com.stofina.app.orderservice.service.impl;

import com.stofina.orderservice.common.ServiceResult;
import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.enums.OrderSide;
import com.stofina.orderservice.enums.OrderStatus;
import com.stofina.orderservice.enums.OrderType;
import com.stofina.orderservice.model.SimpleStopLossWatcher;
import com.stofina.orderservice.repository.OrderRepository;
import com.stofina.orderservice.service.IOrderService;
import com.stofina.orderservice.service.IStopLossService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class StopLossServiceImpl implements IStopLossService {


    private final IOrderService orderService;

    // Thread-safe stop-loss watcher listesi
    private final List<SimpleStopLossWatcher> stopLossWatchers = new CopyOnWriteArrayList<>();

    @Override
    public ServiceResult<Void> addStopLossOrder(Order order) {
        if (!isValidStopLossOrder(order)) {
            return ServiceResult.failure("Geçersiz stop-loss emri: " + getValidationError(order));
        }

        SimpleStopLossWatcher watcher = createWatcher(order);
        stopLossWatchers.add(watcher);

        log.info("Stop-loss watcher eklendi. OrderId: {}, Symbol: {}, TriggerPrice: {}",
                order.getOrderId(), order.getSymbol(), order.getStopPrice());

        return ServiceResult.success(null, "Stop-loss emri takibe alındı.");
    }

    @Override
    public ServiceResult<List<Order>> checkPrice(String symbol, BigDecimal currentPrice) {
        List<Order> triggeredOrders = new ArrayList<>();

        Iterator<SimpleStopLossWatcher> iterator = stopLossWatchers.iterator();
        while (iterator.hasNext()) {
            SimpleStopLossWatcher watcher = iterator.next();

            if (shouldTrigger(watcher, symbol, currentPrice)) {
                Order triggeredOrder = triggerStopLoss(watcher, currentPrice);
                if (triggeredOrder != null) {
                    triggeredOrders.add(triggeredOrder);
                    iterator.remove(); // Tetiklenen watcher'ı listeden çıkar
                }
            } else {
                // Check count'u artır ve son kontrol zamanını güncelle
                updateWatcherActivity(watcher);
            }
        }

        String message = triggeredOrders.isEmpty()
                ? String.format("Symbol %s için hiçbir stop-loss tetiklenmedi. (Fiyat: %s)", symbol, currentPrice)
                : String.format("%d stop-loss emri tetiklendi. (Symbol: %s, Fiyat: %s)",
                triggeredOrders.size(), symbol, currentPrice);

        log.debug(message);
        return ServiceResult.success(triggeredOrders, message);
    }

    @Override
    public ServiceResult<Boolean> remove(Long orderId) {
        boolean removed = stopLossWatchers.removeIf(watcher ->
                Objects.equals(watcher.getOrderId(), orderId));

        if (removed) {
            log.info("Stop-loss watcher silindi. OrderId: {}", orderId);
            return ServiceResult.success(true, "Stop-loss takibi durduruldu.");
        } else {
            log.warn("Silinecek stop-loss watcher bulunamadı. OrderId: {}", orderId);
            return ServiceResult.failure("Stop-loss takibi bulunamadı.");
        }
    }

    @Override
    public ServiceResult<Boolean> isWatching(Long orderId) {
        boolean exists = stopLossWatchers.stream()
                .anyMatch(watcher -> Objects.equals(watcher.getOrderId(), orderId));
        return ServiceResult.success(exists);
    }

    @Override
    public ServiceResult<List<Order>> getAllWatched() {
        List<Order> watchedOrders = stopLossWatchers.stream()
                .map(this::convertWatcherToOrder)
                .toList();

        return ServiceResult.success(watchedOrders,
                String.format("%d stop-loss emri takip ediliyor.", watchedOrders.size()));
    }


    //Belirli sembol için takip edilen stop-loss sayısını döndürür

    public ServiceResult<Integer> getWatchedCountBySymbol(String symbol) {
        int count = (int) stopLossWatchers.stream()
                .filter(watcher -> symbol.equals(watcher.getSymbol()))
                .count();
        return ServiceResult.success(count);
    }

    //Süresi dolmuş watcher'ları temizler (örn: 24 saatten eski)

    public ServiceResult<Integer> cleanupExpiredWatchers() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(1);

        int removedCount = 0;
        Iterator<SimpleStopLossWatcher> iterator = stopLossWatchers.iterator();
        while (iterator.hasNext()) {
            SimpleStopLossWatcher watcher = iterator.next();
            if (watcher.getCreatedAt().isBefore(cutoffTime)) {
                iterator.remove();
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("Süresi dolmuş {} stop-loss watcher temizlendi.", removedCount);
        }

        return ServiceResult.success(removedCount,
                String.format("%d süresi dolmuş watcher temizlendi.", removedCount));
    }


    private boolean isValidStopLossOrder(Order order) {
        return order != null &&
                order.getOrderId() != null &&
                order.getSymbol() != null &&
                !order.getSymbol().trim().isEmpty() &&
                order.getOrderType() == OrderType.STOP_LOSS_SELL &&
                order.getStopPrice() != null &&
                order.getStopPrice().compareTo(BigDecimal.ZERO) > 0 &&
                order.getQuantity() != null &&
                order.getQuantity().compareTo(BigDecimal.ZERO) > 0;
    }

    private String getValidationError(Order order) {
        if (order == null) return "Order null";
        if (order.getOrderId() == null) return "OrderId null";
        if (order.getSymbol() == null || order.getSymbol().trim().isEmpty()) return "Symbol boş";
        if (order.getOrderType() != OrderType.STOP_LOSS_SELL) return "OrderType STOP_LOSS_SELL değil";
        if (order.getStopPrice() == null) return "StopPrice null";
        if (order.getStopPrice().compareTo(BigDecimal.ZERO) <= 0) return "StopPrice <= 0";
        if (order.getQuantity() == null) return "Quantity null";
        if (order.getQuantity().compareTo(BigDecimal.ZERO) <= 0) return "Quantity <= 0";
        return "Bilinmeyen hata";
    }

    private SimpleStopLossWatcher createWatcher(Order order) {
        SimpleStopLossWatcher watcher = new SimpleStopLossWatcher();
        watcher.setOrderId(order.getOrderId());
        watcher.setSymbol(order.getSymbol());
        watcher.setTriggerPrice(order.getStopPrice());
        watcher.setQuantity(order.getQuantity());
        watcher.setAccountId(order.getAccountId());
        watcher.setCreatedAt(LocalDateTime.now());
        watcher.setLastCheckAt(LocalDateTime.now());
        watcher.setCheckCount(0);
        watcher.setTriggered(false);
        return watcher;
    }

    private boolean shouldTrigger(SimpleStopLossWatcher watcher, String symbol, BigDecimal currentPrice) {
        return !watcher.isTriggered() &&
                watcher.getSymbol().equals(symbol) &&
                currentPrice.compareTo(watcher.getTriggerPrice()) <= 0;
    }

    private Order triggerStopLoss(SimpleStopLossWatcher watcher, BigDecimal currentPrice) {
        try {
            // Watcher'ı tetiklenmiş olarak işaretle
            watcher.setTriggered(true);

            // Yeni LIMIT_SELL emri oluştur
            Order limitSellOrder = createLimitSellOrder(watcher, currentPrice);

            // Orijinal order'ın durumunu güncelle
            updateOriginalOrderStatus(watcher.getOrderId(), OrderStatus.PENDING_TRIGGER);

            // Yeni emri sisteme ekle
            ServiceResult<Order> result = orderService.createOrder(limitSellOrder);

            if (result.isSuccess()) {
                log.info("Stop-loss tetiklendi ve LIMIT_SELL emri oluşturuldu. " +
                                "OriginalOrderId: {}, NewOrderId: {}, TriggerPrice: {}, ExecutionPrice: {}",
                        watcher.getOrderId(), result.getData().getOrderId(),
                        watcher.getTriggerPrice(), currentPrice);

                return result.getData();
            } else {
                log.error("Stop-loss tetiklendi ama LIMIT_SELL emri oluşturulamadı. " +
                        "OrderId: {}, Error: {}", watcher.getOrderId(), result.getMessage());
                return null;
            }

        } catch (Exception e) {
            log.error("Stop-loss tetikleme hatası. OrderId: {}", watcher.getOrderId(), e);
            return null;
        }
    }

    private Order createLimitSellOrder(SimpleStopLossWatcher watcher, BigDecimal currentPrice) {
        Order limitSellOrder = new Order();
        limitSellOrder.setSymbol(watcher.getSymbol());
        limitSellOrder.setOrderType(OrderType.LIMIT_SELL);
        limitSellOrder.setSide(OrderSide.SELL);
        limitSellOrder.setQuantity(watcher.getQuantity());
        limitSellOrder.setPrice(currentPrice); // Mevcut fiyattan sat
        limitSellOrder.setAccountId(watcher.getAccountId());
        limitSellOrder.setStatus(OrderStatus.ACTIVE);
        limitSellOrder.setCreatedAt(LocalDateTime.now());
        // Stop-loss referansı için özel alan varsa eklenebilir
        limitSellOrder.setClientOrderId("STOP_TRIGGERED_" + watcher.getOrderId());

        return limitSellOrder;
    }

    private void updateOriginalOrderStatus(Long orderId, OrderStatus newStatus) {
        try {
            orderService.updateOrderStatus(orderId, newStatus);
            log.debug("Orijinal order durumu güncellendi. OrderId: {}, NewStatus: {}", orderId, newStatus);
        } catch (Exception e) {
            log.error("Orijinal order durumu güncellenemedi. OrderId: {}", orderId, e);
        }
    }

    private void updateWatcherActivity(SimpleStopLossWatcher watcher) {
        watcher.setLastCheckAt(LocalDateTime.now());
        watcher.setCheckCount(watcher.getCheckCount() + 1);
    }

    private Order convertWatcherToOrder(SimpleStopLossWatcher watcher) {
        Order order = new Order();
        order.setOrderId(watcher.getOrderId());
        order.setSymbol(watcher.getSymbol());
        order.setOrderType(OrderType.STOP_LOSS_SELL);
        order.setSide(OrderSide.SELL);
        order.setQuantity(watcher.getQuantity());
        order.setStopPrice(watcher.getTriggerPrice());
        order.setAccountId(watcher.getAccountId());
        order.setStatus(OrderStatus.PENDING_TRIGGER);
        order.setCreatedAt(watcher.getCreatedAt());
        return order;
    }
}