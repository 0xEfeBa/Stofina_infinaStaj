package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.common.ServiceResult;
import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.StopLossWatcher;
import com.stofina.app.orderservice.enums.OrderSide;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.enums.OrderType;
import com.stofina.app.orderservice.model.SimpleStopLossWatcher;
import com.stofina.app.orderservice.repository.StopLossWatcherRepository;
import com.stofina.app.orderservice.service.IOrderService;
import com.stofina.app.orderservice.service.IStopLossService;
import com.stofina.app.orderservice.service.client.PortfolioClient;
import com.stofina.app.orderservice.dto.portfolio.SellStockRequest;
import com.stofina.app.orderservice.dto.portfolio.OrderCancellationRequest;
import com.stofina.app.orderservice.dto.portfolio.PortfolioResponse;
import com.stofina.app.orderservice.exception.portfolio.InsufficientStockException;
import com.stofina.app.orderservice.exception.portfolio.PortfolioServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class StopLossServiceImpl implements IStopLossService {

    private final IOrderService orderService;
    private final StopLossWatcherRepository stopLossWatcherRepository;
    
    // CHECKPOINT 3.4 - Portfolio Service integration for stop loss validation
    private final PortfolioClient portfolioClient;

    // Thread-safe in-memory cache (database ile senkronize)
    private final List<SimpleStopLossWatcher> stopLossWatchers = new CopyOnWriteArrayList<>();
    
    // Startup'ta database'den watcher'ları yükle
    @EventListener(ApplicationReadyEvent.class)
    public void loadWatchersFromDatabase() {
        log.info("🔄 Loading stop loss watchers from database...");
        try {
            List<StopLossWatcher> dbWatchers = stopLossWatcherRepository.findByActiveTrueAndTriggeredFalse();
            stopLossWatchers.clear();
            
            for (StopLossWatcher dbWatcher : dbWatchers) {
                SimpleStopLossWatcher simpleWatcher = convertToSimpleWatcher(dbWatcher);
                stopLossWatchers.add(simpleWatcher);
            }
            
            log.info("✅ Loaded {} stop loss watchers from database", stopLossWatchers.size());
        } catch (Exception e) {
            log.error("❌ Failed to load stop loss watchers from database", e);
        }
    }

    @Override
    @Transactional
    public ServiceResult<Void> addStopLossOrder(Order order) {
        if (!isValidStopLossOrder(order)) {
            return ServiceResult.failure("Geçersiz stop-loss emri: " + getValidationError(order));
        }

        try {
            // Database'e kaydet
            StopLossWatcher dbWatcher = createDbWatcher(order);
            StopLossWatcher savedWatcher = stopLossWatcherRepository.save(dbWatcher);
            
            // In-memory cache'e ekle
            SimpleStopLossWatcher simpleWatcher = convertToSimpleWatcher(savedWatcher);
            stopLossWatchers.add(simpleWatcher);

            log.info("Stop-loss watcher eklendi. WatcherId: {}, OrderId: {}, Symbol: {}, TriggerPrice: {}",
                    savedWatcher.getWatcherId(), order.getOrderId(), order.getSymbol(), order.getStopPrice());

            return ServiceResult.success(null, "Stop-loss emri takibe alındı.");
        } catch (Exception e) {
            log.error("Stop-loss watcher eklenirken hata oluştu. OrderId: {}", order.getOrderId(), e);
            return ServiceResult.failure("Stop-loss watcher eklenemedi: " + e.getMessage());
        }
    }

    @Override
    public ServiceResult<List<Order>> checkPrice(String symbol, BigDecimal currentPrice) {
        List<Order> triggeredOrders = new ArrayList<>();

        log.info("🔍 STOP LOSS ENTRY - Symbol: {}, Price: {}, Total Watchers: {}", 
                 symbol, currentPrice, stopLossWatchers.size());
        
        Iterator<SimpleStopLossWatcher> iterator = stopLossWatchers.iterator();
        int watcherIndex = 0;
        while (iterator.hasNext()) {
            SimpleStopLossWatcher watcher = iterator.next();
            watcherIndex++;
            
            log.info("🔍 CHECKING WATCHER #{} - OrderId: {}, Symbol: {}, TriggerPrice: {}", 
                     watcherIndex, watcher.getOrderId(), watcher.getSymbol(), watcher.getTriggerPrice());

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

        log.info("🔍 STOP LOSS EXIT - {}", message);
        return ServiceResult.success(triggeredOrders, message);
    }

    @Override
    @Transactional
    public ServiceResult<Boolean> remove(Long orderId) {
        try {
            // Database'den soft delete
            Optional<StopLossWatcher> dbWatcher = stopLossWatcherRepository.findByOrderIdAndActiveTrue(orderId);
            if (dbWatcher.isPresent()) {
                StopLossWatcher watcher = dbWatcher.get();
                watcher.setActive(false);
                stopLossWatcherRepository.save(watcher);
                
                // In-memory cache'den kaldır
                boolean removed = stopLossWatchers.removeIf(w -> Objects.equals(w.getOrderId(), orderId));
                
                log.info("Stop-loss watcher silindi. WatcherId: {}, OrderId: {}", watcher.getWatcherId(), orderId);
                return ServiceResult.success(true, "Stop-loss takibi durduruldu.");
            } else {
                log.warn("Silinecek stop-loss watcher bulunamadı. OrderId: {}", orderId);
                return ServiceResult.failure("Stop-loss takibi bulunamadı.");
            }
        } catch (Exception e) {
            log.error("Stop-loss watcher silinirken hata oluştu. OrderId: {}", orderId, e);
            return ServiceResult.failure("Stop-loss watcher silinemedi: " + e.getMessage());
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
        boolean isNotTriggered = !watcher.isTriggered();
        boolean symbolMatch = watcher.getSymbol().equals(symbol);
        boolean priceBelow = currentPrice.compareTo(watcher.getTriggerPrice()) <= 0;
        
        log.info("🔍 STOP LOSS DEBUG - OrderId: {}, Symbol: {} vs {}, Price: {} vs {}, NotTriggered: {}, SymbolMatch: {}, PriceBelow: {}", 
                 watcher.getOrderId(), symbol, watcher.getSymbol(), currentPrice, watcher.getTriggerPrice(), 
                 isNotTriggered, symbolMatch, priceBelow);
        
        return isNotTriggered && symbolMatch && priceBelow;
    }

    @Transactional
    public Order triggerStopLoss(SimpleStopLossWatcher watcher, BigDecimal currentPrice) {
        try {
            // Database'de watcher'ı tetiklenmiş olarak işaretle
            Optional<StopLossWatcher> dbWatcherOpt = stopLossWatcherRepository.findByOrderIdAndActiveTrue(watcher.getOrderId());
            if (dbWatcherOpt.isPresent()) {
                StopLossWatcher dbWatcher = dbWatcherOpt.get();
                dbWatcher.setTriggered(true);
                stopLossWatcherRepository.save(dbWatcher);
            }
            
            // In-memory watcher'ı da güncelle
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
        // CHECKPOINT 3.4 - Portfolio validation before creating stop-loss sell order
        if (!validateStopLossPortfolio(watcher)) {
            log.error("🛑 STOP LOSS: Portfolio validation failed → OrderId: {}, AccountId: {}, Symbol: {}, Quantity: {}", 
                    watcher.getOrderId(), watcher.getAccountId(), watcher.getSymbol(), watcher.getQuantity());
            return null; // Return null if portfolio validation fails
        }

        Order limitSellOrder = new Order();
        limitSellOrder.setSymbol(watcher.getSymbol());
        limitSellOrder.setOrderType(OrderType.LIMIT_SELL);
        limitSellOrder.setSide(OrderSide.SELL);
        limitSellOrder.setQuantity(watcher.getQuantity());
        limitSellOrder.setPrice(currentPrice); // Mevcut fiyattan sat
        limitSellOrder.setAccountId(watcher.getAccountId());
        limitSellOrder.setTenantId(watcher.getTenantId()); // TenantId ekle!
        limitSellOrder.setStatus(OrderStatus.ACTIVE);
        limitSellOrder.setCreatedAt(LocalDateTime.now());
        // Stop-loss referansı için özel alan varsa eklenebilir
        limitSellOrder.setClientOrderId("STOP_TRIGGERED_" + watcher.getOrderId());

        log.info("🛑 STOP LOSS: Created limit sell order after portfolio validation → OrderId: {}, Symbol: {}, Quantity: {}, Price: {}", 
                watcher.getOrderId(), limitSellOrder.getSymbol(), limitSellOrder.getQuantity(), currentPrice);

        return limitSellOrder;
    }

    /**
     * Validates that the account has sufficient stock quantity before creating a stop-loss sell order.
     * This prevents creating stop-loss orders for stocks that the account doesn't own.
     * @param watcher The stop-loss watcher containing order details
     * @return true if account has sufficient stock, false otherwise
     */
    private boolean validateStopLossPortfolio(SimpleStopLossWatcher watcher) {
        try {
            log.info("🛑 STOP LOSS: Validating portfolio → AccountId: {}, Symbol: {}, RequiredQuantity: {}", 
                    watcher.getAccountId(), watcher.getSymbol(), watcher.getQuantity());

            // Create sell stock validation request
            SellStockRequest validationRequest = SellStockRequest.builder()
                    .orderId(watcher.getOrderId())
                    .accountId(watcher.getAccountId())
                    .symbol(watcher.getSymbol())
                    .quantity(watcher.getQuantity().intValue())
                    .build();

            // Call Portfolio Service for stock position validation
            CompletableFuture<PortfolioResponse> validationFuture = portfolioClient.reserveSellStock(validationRequest);
            PortfolioResponse response = validationFuture.get();

            if (response.isSuccess()) {
                log.info("✅ STOP LOSS: Portfolio validation successful → AccountId: {}, Symbol: {}, Message: {}", 
                        watcher.getAccountId(), watcher.getSymbol(), response.getMessage());
                
                // Important: Cancel the reservation immediately as this was just for validation
                OrderCancellationRequest cancellationRequest = OrderCancellationRequest.builder()
                        .orderId(watcher.getOrderId())
                        .accountId(watcher.getAccountId())
                        .symbol(watcher.getSymbol())
                        .orderType(OrderType.LIMIT_SELL)
                        .originalQuantity(watcher.getQuantity().intValue())
                        .filledQuantity(0) // No quantity was actually filled, this was just validation
                        .reason("Stop-loss portfolio validation - reservation cancelled")
                        .build();
                
                // Cancel the validation reservation asynchronously (no need to wait)
                portfolioClient.cancelSellOrder(cancellationRequest)
                        .thenAccept(cancelResponse -> {
                            if (cancelResponse.isSuccess()) {
                                log.debug("🛑 STOP LOSS: Validation reservation cancelled → OrderId: {}", watcher.getOrderId());
                            } else {
                                log.warn("🛑 STOP LOSS: Failed to cancel validation reservation → OrderId: {}, Error: {}", 
                                        watcher.getOrderId(), cancelResponse.getMessage());
                            }
                        })
                        .exceptionally(ex -> {
                            log.error("🛑 STOP LOSS: Error cancelling validation reservation → OrderId: {}", 
                                    watcher.getOrderId(), ex);
                            return null;
                        });
                
                return true;
            } else {
                log.warn("🛑 STOP LOSS: Portfolio validation failed → AccountId: {}, Symbol: {}, Error: {}", 
                        watcher.getAccountId(), watcher.getSymbol(), response.getMessage());
                return false;
            }

        } catch (ExecutionException e) {
            if (e.getCause() instanceof InsufficientStockException) {
                log.warn("🛑 STOP LOSS: Insufficient stock for stop-loss → AccountId: {}, Symbol: {}, RequiredQuantity: {}", 
                        watcher.getAccountId(), watcher.getSymbol(), watcher.getQuantity());
                return false;
            } else if (e.getCause() instanceof PortfolioServiceException portfolioEx) {
                log.error("🛑 STOP LOSS: Portfolio service error during validation → ErrorCode: {}, Message: {}", 
                        portfolioEx.getErrorCode(), portfolioEx.getMessage());
                return false;
            } else {
                log.error("🛑 STOP LOSS: Execution error during portfolio validation → AccountId: {}, Symbol: {}", 
                        watcher.getAccountId(), watcher.getSymbol(), e);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("🛑 STOP LOSS: Portfolio validation interrupted → AccountId: {}, Symbol: {}", 
                    watcher.getAccountId(), watcher.getSymbol(), e);
            return false;
        } catch (Exception e) {
            log.error("🛑 STOP LOSS: Unexpected error during portfolio validation → AccountId: {}, Symbol: {}", 
                    watcher.getAccountId(), watcher.getSymbol(), e);
            return false;
        }
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
    
    // Database Entity -> SimpleStopLossWatcher converter
    private SimpleStopLossWatcher convertToSimpleWatcher(StopLossWatcher dbWatcher) {
        SimpleStopLossWatcher simpleWatcher = new SimpleStopLossWatcher();
        simpleWatcher.setOrderId(dbWatcher.getOrderId());
        simpleWatcher.setSymbol(dbWatcher.getSymbol());
        simpleWatcher.setTriggerPrice(dbWatcher.getTriggerPrice());
        simpleWatcher.setQuantity(dbWatcher.getQuantity());
        simpleWatcher.setAccountId(dbWatcher.getAccountId());
        simpleWatcher.setTenantId(dbWatcher.getTenantId());
        simpleWatcher.setCreatedAt(dbWatcher.getCreatedAt());
        simpleWatcher.setLastCheckAt(dbWatcher.getLastCheckAt());
        simpleWatcher.setCheckCount(dbWatcher.getCheckCount());
        simpleWatcher.setTriggered(dbWatcher.getTriggered());
        return simpleWatcher;
    }
    
    // Order -> Database Entity converter
    private StopLossWatcher createDbWatcher(Order order) {
        StopLossWatcher dbWatcher = new StopLossWatcher();
        dbWatcher.setOrderId(order.getOrderId());
        dbWatcher.setSymbol(order.getSymbol());
        dbWatcher.setTriggerPrice(order.getStopPrice());
        dbWatcher.setQuantity(order.getQuantity());
        dbWatcher.setAccountId(order.getAccountId());
        dbWatcher.setTenantId(order.getTenantId());
        dbWatcher.setCreatedAt(LocalDateTime.now());
        dbWatcher.setLastCheckAt(LocalDateTime.now());
        dbWatcher.setCheckCount(0);
        dbWatcher.setTriggered(false);
        dbWatcher.setActive(true);
        return dbWatcher;
    }
}