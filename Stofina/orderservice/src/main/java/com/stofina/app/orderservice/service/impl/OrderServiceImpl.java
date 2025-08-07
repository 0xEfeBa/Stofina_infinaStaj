package com.stofina.app.orderservice.service.impl;

import com.stofina.orderservice.dto.request.CreateOrderRequest;
import com.stofina.orderservice.dto.request.OrderFilterRequest;
import com.stofina.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.orderservice.dto.response.OrderResponse;
import com.stofina.orderservice.exception.OrderNotFoundException;
import com.stofina.orderservice.mapper.OrderMapper;
import com.stofina.orderservice.repository.OrderRepository;
import com.stofina.orderservice.repository.TradeRepository;
import com.stofina.orderservice.service.OrderService;
import com.stofina.orderservice.service.ValidationService;
import com.stofina.orderservice.service.client.MarketDataClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final TradeRepository tradeRepository;
    private final OrderRepository orderRepository;
    private final ValidationService validationService;
    private final MarketDataClient marketDataClient;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        validationService.validateOrderRequest(request);

        Order order = orderMapper.toEntity(request);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("NEW");

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public OrderResponse updateOrder(Long orderId, UpdateOrderRequest request) {
        Order existing = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        validationService.validateOrderUpdate(existing, request);

        orderMapper.updateEntity(existing, request);
        existing.setUpdatedAt(LocalDateTime.now());

        Order updated = orderRepository.save(existing);

        return orderMapper.toResponse(updated);
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        order.setStatus("CANCELED");
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    @Override
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrders(OrderFilterRequest filter) {
        // özel query yazmam lazım
    }

    @Override
    public List<OrderResponse> getActiveOrdersBySymbol(String symbol) {
        List<Order> orders = orderRepository.findBySymbolAndStatus(symbol, "ACTIVE");
        return orderMapper.toResponseList(orders);
    }

    @Override
    public int processExpiredOrders() {
        List<Order> expiredOrders = orderRepository.findByExpiryDateBeforeAndStatus(LocalDateTime.now(), "NEW");
        expiredOrders.forEach(order -> order.setStatus("EXPIRED"));
        orderRepository.saveAll(expiredOrders);
        return expiredOrders.size();
    }

    public void validateOrderRequest(CreateOrderRequest request) {
        validationService.validateOrderRequest(request);
    }

    public void checkOrderPermissions(Order order) {
    }
}
