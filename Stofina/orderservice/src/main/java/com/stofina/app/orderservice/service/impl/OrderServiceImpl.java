package com.stofina.app.orderservice.service.impl;

import com.stofina.orderservice.common.ServiceResult;
import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.enums.OrderStatus;
import com.stofina.orderservice.enums.OrderType;
import com.stofina.orderservice.repository.OrderRepository;
import com.stofina.orderservice.service.IOrderService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void executeMarketOrder(Order order) {
        if (order == null || order.getOrderId() == null) {
            log.warn("Geçersiz market emri: null order");
            return;
        }

        if (order.getOrderType() != OrderType.MARKET_BUY || order.getOrderType() != OrderType.MARKET_SELL) {
            log.warn("Market emri değil: OrderId={}, Type={}", order.getOrderId(), order.getOrderType());
            return;
        }

        // Basit simülasyon: Tüm miktar sabit fiyattan eşleşti varsay
        BigDecimal simulatedPrice = mockCurrentPrice(order.getSymbol());
        BigDecimal total = simulatedPrice.multiply(order.getQuantity());

        order.setFilledQuantity(order.getQuantity());
        order.setAveragePrice(simulatedPrice);
        order.setStatus(OrderStatus.FILLED);

        log.info("MARKET order executed → OrderId: {}, Price: {}, Quantity: {}, Total: {}",
                order.getOrderId(), simulatedPrice, order.getQuantity(), total);
    }

    private BigDecimal mockCurrentPrice(String symbol) {
        return BigDecimal.valueOf(75.00); // test için sabit fiyat döndürülüyor
    }

    @Override
    @Transactional
    public ServiceResult<Order> createOrder(Order order) {
        try {
            Order savedOrder = orderRepository.save(order);
            log.info("Yeni emir oluşturuldu. OrderId: {}", savedOrder.getOrderId());
            return ServiceResult.success(savedOrder, "Emir başarıyla oluşturuldu.");
        } catch (Exception e) {
            log.error("Emir oluşturulurken hata oluştu: {}", e.getMessage(), e);
            return ServiceResult.failure("Emir oluşturulamadı: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResult<Void> updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isEmpty()) {
            log.warn("Order bulunamadı. OrderId: {}", orderId);
            return ServiceResult.failure("Order bulunamadı.");
        }

        try {
            Order order = optionalOrder.get();
            order.setStatus(newStatus);
            orderRepository.save(order); // update işlemi

            log.info("Order durumu güncellendi. OrderId: {}, Yeni Durum: {}", orderId, newStatus);
            return ServiceResult.success(null, "Order durumu güncellendi.");
        } catch (Exception e) {
            log.error("Order durumu güncellenemedi. OrderId: {}, Hata: {}", orderId, e.getMessage(), e);
            return ServiceResult.failure("Durum güncellenemedi: " + e.getMessage());
        }
    }

}