package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.common.ServiceResult;
import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.repository.OrderRepository;
import com.stofina.app.orderservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class IOrderServiceImpl implements IOrderService {
    
    private final OrderRepository orderRepository;
    
    @Override
    public void executeMarketOrder(Order order) {
        // Market order execution logic
        log.info("Executing market order: {}", order.getOrderId());
        order.setStatus(OrderStatus.FILLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
    
    @Override
    public ServiceResult<Void> updateOrderStatus(Long orderId, OrderStatus newStatus) {
        try {
            int updated = orderRepository.updateOrderStatus(orderId, newStatus);
            if (updated > 0) {
                return ServiceResult.<Void>builder().success(true).build();
            } else {
                return ServiceResult.<Void>failure("Order not found: " + orderId);
            }
        } catch (Exception e) {
            log.error("Failed to update order status: {}", e.getMessage());
            return ServiceResult.<Void>failure("Failed to update order status: " + e.getMessage());
        }
    }
    
    @Override
    public ServiceResult<Order> createOrder(Order order) {
        try {
            order.setStatus(OrderStatus.NEW);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            
            Order savedOrder = orderRepository.save(order);
            return ServiceResult.success(savedOrder);
        } catch (Exception e) {
            log.error("Failed to create order: {}", e.getMessage());
            return ServiceResult.<Order>failure("Failed to create order: " + e.getMessage());
        }
    }
}