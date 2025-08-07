package com.stofina.orderservice.service.impl;

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
import com.stofina.orderservice.service.AlgorithmicMatchingService;
import com.stofina.orderservice.service.DisplayOrderBookService;
import com.stofina.orderservice.service.SimpleOrderBookManager;
import com.stofina.orderservice.service.client.MarketDataClient;

import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.entity.Trade;
import com.stofina.orderservice.enums.OrderStatus;
import com.stofina.orderservice.enums.OrderType;
import com.stofina.orderservice.service.IStopLossService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final TradeRepository tradeRepository;
    private final OrderRepository orderRepository;
    private final ValidationService validationService;
    private final MarketDataClient marketDataClient;
    private final OrderMapper orderMapper;
    private final DisplayOrderBookService displayOrderBookService;
    private final SimpleOrderBookManager simpleOrderBookManager;
    private final AlgorithmicMatchingService algorithmicMatchingService;
    private final IStopLossService stopLossService;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("🔄 LIFECYCLE-2: OrderService.createOrder() - ENTRY - Validating request");
        validationService.validateOrderRequest(request);

        Order order = orderMapper.toEntity(request);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.setFilledQuantity(BigDecimal.ZERO);
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("🔄 LIFECYCLE-2: OrderService - Order saved to DB - ID={}, Quantity={}, FilledQuantity={}, RemainingQuantity={}", 
                savedOrder.getOrderId(), savedOrder.getQuantity(), savedOrder.getFilledQuantity(), savedOrder.getRemainingQuantity());

        // CHECKPOINT ENTEGRASYON 1.1 - DisplayOrderBook'a user order ekle
        log.info("🔄 LIFECYCLE-2: OrderService - Calling DisplayOrderBookService.addUserOrderToDisplay()");
        displayOrderBookService.addUserOrderToDisplay(savedOrder);
        log.info("🔄 LIFECYCLE-2: OrderService - Order added to display book successfully");
        
        // CHECKPOINT ENTEGRASYON 1.2 - Immediate matching tetikleme ve trade kaydetme
        log.info("🔄 LIFECYCLE-2: OrderService - Calling SimpleOrderBookManager.addOrder() - CRITICAL CALL");
        List<Trade> trades = simpleOrderBookManager.addOrder(savedOrder);
        log.info("🔄 LIFECYCLE-2: OrderService - SimpleOrderBookManager returned {} trades", trades.size());
        
        // Log trade information
        if (!trades.isEmpty()) {
            log.info("Order {} generated {} trades", savedOrder.getOrderId(), trades.size());
            for (Trade trade : trades) {
                log.info("Trade executed: {} {} @ {} - Trade ID: {}", 
                        trade.getQuantity(), trade.getSymbol(), trade.getPrice(), trade.getTradeId());
            }
        }
        
        // STOP LOSS ENTEGRASYONU - STOP_LOSS_SELL emirlerini takibe al
        if (savedOrder.getOrderType() == OrderType.STOP_LOSS_SELL) {
            log.info("🛑 STOP LOSS: Adding STOP_LOSS_SELL order to watcher system - OrderId: {}", savedOrder.getOrderId());
            try {
                stopLossService.addStopLossOrder(savedOrder);
                log.info("✅ STOP LOSS: Order successfully added to watcher system - OrderId: {}", savedOrder.getOrderId());
            } catch (Exception e) {
                log.error("❌ STOP LOSS: Failed to add order to watcher system - OrderId: {}", savedOrder.getOrderId(), e);
            }
        }
        
        // CHECKPOINT C4 - Log algorithmic matching eligibility
        if (savedOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
            int algorithmicCount = algorithmicMatchingService.getAlgorithmicMatchingCount(savedOrder.getOrderId());
            log.info("Order {} has remaining quantity {} - Algorithmic matching count: {}/2", 
                    savedOrder.getOrderId(), savedOrder.getRemainingQuantity(), algorithmicCount);
        }

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

        order.setStatus(OrderStatus.CANCELLED);
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
        // TODO: Implement order filtering
        Page<Order> orders = orderRepository.findAll(PageRequest.of(0, 10));
        List<OrderResponse> responses = orderMapper.toResponseList(orders.getContent());
        return new org.springframework.data.domain.PageImpl<>(responses, orders.getPageable(), orders.getTotalElements());
    }

    @Override
    public List<OrderResponse> getActiveOrdersBySymbol(String symbol) {
        List<Order> orders = orderRepository.findBySymbolAndStatusIn(symbol, List.of(OrderStatus.NEW, OrderStatus.PARTIALLY_FILLED));
        return orderMapper.toResponseList(orders);
    }

    @Override
    public List<OrderResponse> getOrdersByAccount(Long accountId) {
        List<Order> orders = orderRepository.findByAccountIdAndStatus(accountId, OrderStatus.NEW);
        return orderMapper.toResponseList(orders);
    }

    @Override
    public Map<String, Object> validateOrder(CreateOrderRequest request) {
        try {
            validationService.validateOrderRequest(request);
            return Map.of("valid", true, "message", "Order is valid");
        } catch (Exception e) {
            return Map.of("valid", false, "message", e.getMessage());
        }
    }

    @Override
    public int processExpiredOrders() {
        // TODO: Implement expired orders processing
        return 0;
    }

    public void validateOrderRequest(CreateOrderRequest request) {
        validationService.validateOrderRequest(request);
    }

    public void checkOrderPermissions(Order order) {
    //*********************
    }
}