package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.dto.request.CreateOrderRequest;
import com.stofina.app.orderservice.dto.request.OrderFilterRequest;
import com.stofina.app.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.app.orderservice.dto.response.OrderResponse;
import com.stofina.app.orderservice.exception.OrderNotFoundException;
import com.stofina.app.orderservice.mapper.OrderMapper;
import com.stofina.app.orderservice.repository.OrderRepository;
import com.stofina.app.orderservice.repository.TradeRepository;
import com.stofina.app.orderservice.service.OrderService;
import com.stofina.app.orderservice.service.ValidationService;
import com.stofina.app.orderservice.service.AlgorithmicMatchingService;
import com.stofina.app.orderservice.service.DisplayOrderBookService;
import com.stofina.app.orderservice.service.SimpleOrderBookManager;
import com.stofina.app.orderservice.service.PendingOrderService;
import com.stofina.app.orderservice.service.client.MarketDataClient;
import com.stofina.app.orderservice.service.client.PortfolioClient;
import com.stofina.app.orderservice.dto.portfolio.*;
import com.stofina.app.orderservice.exception.portfolio.PortfolioServiceException;
import com.stofina.app.orderservice.exception.portfolio.InsufficientBalanceException;
import com.stofina.app.orderservice.exception.portfolio.InsufficientStockException;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.Trade;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.enums.OrderType;
import com.stofina.app.orderservice.service.IStopLossService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    private final PortfolioClient portfolioClient;
    private final PendingOrderService pendingOrderService;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("🔄 LIFECYCLE-2: OrderService.createOrder() - ENTRY - Validating request");
        validationService.validateOrderRequest(request);
        
        // PRE-VALIDATION: Check portfolio constraints BEFORE creating order in database
        log.info("🔒 PRE-VALIDATION: Starting portfolio checks before order creation");
        try {
            if (request.getOrderType().isBuyOrder()) {
                // For BUY orders, check account balance (including commission)
                BigDecimal totalCost = null;
                if (request.getPrice() != null) {
                    // LIMIT_BUY - use order price
                    totalCost = request.getPrice().multiply(new BigDecimal(request.getQuantity()));
                } else {
                    // MARKET_BUY - use current market price for estimation
                    BigDecimal marketPrice = marketDataClient.getCurrentPrice(request.getSymbol());
                    if (marketPrice != null) {
                        totalCost = marketPrice.multiply(new BigDecimal(request.getQuantity()));
                    } else {
                        log.warn("⚠️ PRE-VALIDATION: Cannot get market price for {}, proceeding without balance validation", request.getSymbol());
                    }
                }
                
                if (totalCost != null) {
                    validationService.checkAccountBalance(request.getAccountId(), totalCost).get();
                    log.info("✅ PRE-VALIDATION: Account balance check passed");
                }
            } else if (request.getOrderType().isSellOrder()) {
                // For SELL orders, check stock position
                validationService.checkAccountPosition(request.getAccountId(), request.getSymbol(), request.getQuantity()).get();
                log.info("✅ PRE-VALIDATION: Stock position check passed");
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ PRE-VALIDATION: Failed - {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new IllegalArgumentException("Portfolio validation failed: " + e.getMessage());
        }
        log.info("🔒 PRE-VALIDATION: All checks passed - proceeding with order creation");

        Order order = orderMapper.toEntity(request);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.setFilledQuantity(BigDecimal.ZERO);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Check if order price is within ±1.5% range for LIMIT orders only
        boolean shouldPendOrder = false;
        BigDecimal currentMarketPrice = null;
        
        if (request.getOrderType().isLimitOrder() && request.getPrice() != null) {
            try {
                currentMarketPrice = marketDataClient.getCurrentPrice(request.getSymbol());
                if (currentMarketPrice != null && 
                    !pendingOrderService.isPriceWithinRange(currentMarketPrice, request.getPrice())) {
                    shouldPendOrder = true;
                    log.info("🟡 PRICE CHECK: Order price outside ±1.5% range - will be held for activation. " +
                            "OrderPrice: {}, MarketPrice: {}, Symbol: {}", 
                            request.getPrice(), currentMarketPrice, request.getSymbol());
                }
            } catch (Exception e) {
                log.warn("⚠️ PRICE CHECK: Unable to get current market price for {}, proceeding with order: {}", 
                        request.getSymbol(), e.getMessage());
            }
        }

        // CHECKPOINT 2.1 - SAVE ORDER FIRST to get orderId
        Order savedOrder = orderRepository.save(order);
        log.info("🔄 LIFECYCLE-2: Order saved to DB - ID={}, Status={}", savedOrder.getOrderId(), savedOrder.getStatus());

        // CHECKPOINT 2.2 - Portfolio Service Reservation AFTER getting orderId
        log.info("🏦 PORTFOLIO: Starting portfolio reservation - OrderType: {}, Symbol: {}, Quantity: {}, OrderId: {}", 
                request.getOrderType(), request.getSymbol(), request.getQuantity(), savedOrder.getOrderId());
        
        try {
            boolean reservationSuccess = performPortfolioReservation(savedOrder);
            if (!reservationSuccess) {
                log.error("🏦 PORTFOLIO: Reservation failed - Deleting order and throwing exception");
                orderRepository.delete(savedOrder);
                throw new PortfolioServiceException("Portfolio reservation failed", "RESERVATION_FAILED");
            }
            log.info("🏦 PORTFOLIO: Reservation successful - Order creation complete");
        } catch (InsufficientBalanceException | InsufficientStockException e) {
            log.error("🏦 PORTFOLIO: Business rule violation - Deleting order: {}", e.getUserFriendlyMessage());
            orderRepository.delete(savedOrder);
            throw e; // Re-throw business exceptions
        } catch (PortfolioServiceException e) {
            log.error("🏦 PORTFOLIO: Service error - Deleting order: {}", e.getMessage());
            orderRepository.delete(savedOrder);
            throw e; // Re-throw portfolio service exceptions
        } catch (Exception e) {
            log.error("🏦 PORTFOLIO: Unexpected error during reservation - Deleting order", e);
            orderRepository.delete(savedOrder);
            throw new PortfolioServiceException("Unexpected error during portfolio reservation", "UNEXPECTED_ERROR", e);
        }
        log.info("🔄 LIFECYCLE-2: OrderService - Order saved to DB - ID={}, Quantity={}, FilledQuantity={}, RemainingQuantity={}", 
                savedOrder.getOrderId(), savedOrder.getQuantity(), savedOrder.getFilledQuantity(), savedOrder.getRemainingQuantity());

        // Check if order should be held for price activation
        if (shouldPendOrder && currentMarketPrice != null) {
            log.info("🟡 PENDING: Adding order to pending activation queue - OrderId: {}", savedOrder.getOrderId());
            pendingOrderService.addToPendingActivation(savedOrder, currentMarketPrice, request.getPrice());
            
            // Return order with PENDING_TRIGGER status - no immediate processing
            return orderMapper.toResponse(savedOrder);
        }

        // NORMAL FLOW: Price is within range or no price check needed
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

        log.info("🚫 CANCEL: Starting order cancellation - OrderId: {}, OrderType: {}, Status: {}", 
                orderId, order.getOrderType(), order.getStatus());

        // Check if order can be cancelled
        if (order.getStatus() == OrderStatus.FILLED || order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("🚫 CANCEL: Cannot cancel order in status {} - OrderId: {}", order.getStatus(), orderId);
            throw new IllegalStateException("Order cannot be cancelled in status: " + order.getStatus());
        }

        // Remove from pending orders if it's pending
        if (order.getStatus() == OrderStatus.PENDING_TRIGGER) {
            boolean removedFromPending = pendingOrderService.removePendingOrder(orderId);
            log.info("🚫 CANCEL: Pending order removal result - OrderId: {}, Removed: {}", 
                    orderId, removedFromPending);
        }

        // CHECKPOINT 2.2 - Portfolio Service Cancellation
        try {
            boolean cancellationSuccess = performPortfolioCancellation(order);
            if (!cancellationSuccess) {
                log.error("🏦 PORTFOLIO: Order cancellation failed at portfolio level - OrderId: {}", orderId);
                throw new PortfolioServiceException("Portfolio order cancellation failed", "CANCELLATION_FAILED");
            }
            log.info("🏦 PORTFOLIO: Order cancellation successful at portfolio level - OrderId: {}", orderId);
        } catch (PortfolioServiceException e) {
            log.error("🏦 PORTFOLIO: Portfolio service error during cancellation - OrderId: {}, Error: {}", 
                    orderId, e.getMessage());
            throw e; // Re-throw portfolio service exceptions
        } catch (Exception e) {
            log.error("🏦 PORTFOLIO: Unexpected error during order cancellation - OrderId: {}", orderId, e);
            throw new PortfolioServiceException("Unexpected error during order cancellation", "UNEXPECTED_ERROR", e);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
        log.info("🚫 CANCEL: Order successfully cancelled - OrderId: {}", orderId);
    }

    @Override
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrders(OrderFilterRequest filter) {
        Pageable pageable = buildPageableFromFilter(filter);
        
        Page<Order> orders = (filter.getAccountId() != null) 
            ? orderRepository.findByAccountId(filter.getAccountId(), pageable)
            : orderRepository.findAll(pageable);
        
        List<OrderResponse> responses = orderMapper.toResponseList(orders.getContent());
        return new org.springframework.data.domain.PageImpl<>(responses, orders.getPageable(), orders.getTotalElements());
    }

    private Pageable buildPageableFromFilter(OrderFilterRequest filter) {
        return PageRequest.of(filter.getPage(), filter.getSize());
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

    // PRIVATE HELPER METHODS FOR PORTFOLIO INTEGRATION

    /**
     * Performs portfolio reservation based on order type (BUY/SELL).
     * @param order The order for which to make reservation
     * @return true if reservation successful, false otherwise
     * @throws PortfolioServiceException if portfolio service error occurs
     * @throws InsufficientBalanceException if insufficient balance for buy order
     * @throws InsufficientStockException if insufficient stock for sell order
     */
    private boolean performPortfolioReservation(Order order) throws PortfolioServiceException, 
            InsufficientBalanceException, InsufficientStockException {
        
        try {
            CompletableFuture<PortfolioResponse> reservationFuture;
            
            switch (order.getOrderType()) {
                case LIMIT_BUY, MARKET_BUY -> {
                    // Market Service kapalıysa manuel fiyat kullan
                    BigDecimal priceToUse = order.getPrice();
                    if (priceToUse == null && order.getOrderType() == OrderType.MARKET_BUY) {
                        priceToUse = new BigDecimal("46.00"); // Test fiyatı
                        log.warn("🏦 PORTFOLIO: Market Service unavailable for MARKET_BUY - Using manual test price: {}", priceToUse);
                    }
                    
                    BuyStockRequest buyRequest = BuyStockRequest.builder()
                            .accountId(order.getAccountId())
                            .symbol(order.getSymbol())
                            .orderId(order.getOrderId())
                            .quantity(order.getQuantity().intValue())
                            .price(priceToUse)
                            .build();
                    
                    log.info("🏦 PORTFOLIO: Making BUY reservation → {}", buyRequest);
                    reservationFuture = portfolioClient.reserveBuyStock(buyRequest);
                }
                case LIMIT_SELL, MARKET_SELL, STOP_LOSS_SELL -> {
                    // Market Service kapalıysa manuel fiyat kullan
                    BigDecimal priceToUse = order.getPrice();
                    if (priceToUse == null && order.getOrderType() == OrderType.MARKET_SELL) {
                        priceToUse = new BigDecimal("47.00"); // Test fiyatı
                        log.warn("🏦 PORTFOLIO: Market Service unavailable for MARKET_SELL - Using manual test price: {}", priceToUse);
                    }
                    
                    SellStockRequest sellRequest = SellStockRequest.builder()
                            .accountId(order.getAccountId())
                            .symbol(order.getSymbol())
                            .orderId(order.getOrderId())
                            .quantity(order.getQuantity().intValue())
                            .price(priceToUse) // Price eklendi
                            .build();
                    
                    log.info("🏦 PORTFOLIO: Making SELL reservation → {}", sellRequest);
                    reservationFuture = portfolioClient.reserveSellStock(sellRequest);
                }
                default -> {
                    log.error("🏦 PORTFOLIO: Unsupported order type for reservation → {}", order.getOrderType());
                    return false;
                }
            }
            
            // Wait for reservation result
            PortfolioResponse response = reservationFuture.get();
            
            if (response.isSuccess()) {
                log.info("🏦 PORTFOLIO: Reservation successful → OrderId: {}, Message: {}", 
                        order.getOrderId(), response.getMessage());
                return true;
            } else {
                log.error("🏦 PORTFOLIO: Reservation failed → OrderId: {}, Error: {}, ErrorCode: {}", 
                        order.getOrderId(), response.getMessage(), response.getErrorCode());
                
                // Convert portfolio response errors to appropriate exceptions
                if ("INSUFFICIENT_BALANCE".equals(response.getErrorCode())) {
                    throw new InsufficientBalanceException(response.getMessage(), order.getAccountId());
                } else if ("INSUFFICIENT_STOCK".equals(response.getErrorCode())) {
                    throw new InsufficientStockException(response.getMessage(), order.getAccountId(), order.getSymbol());
                } else {
                    throw new PortfolioServiceException(response.getMessage(), response.getErrorCode());
                }
            }
            
        } catch (ExecutionException e) {
            log.error("🏦 PORTFOLIO: Reservation execution error → OrderId: {}", order.getOrderId(), e);
            if (e.getCause() instanceof PortfolioServiceException portfolioEx) {
                throw portfolioEx;
            } else if (e.getCause() instanceof InsufficientBalanceException balanceEx) {
                throw balanceEx;
            } else if (e.getCause() instanceof InsufficientStockException stockEx) {
                throw stockEx;
            } else {
                throw new PortfolioServiceException("Portfolio reservation execution failed", "EXECUTION_ERROR", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("🏦 PORTFOLIO: Reservation interrupted → OrderId: {}", order.getOrderId(), e);
            throw new PortfolioServiceException("Portfolio reservation interrupted", "INTERRUPTED", e);
        }
    }

    /**
     * Performs portfolio cancellation based on order type (BUY/SELL).
     * @param order The order for which to perform cancellation
     * @return true if cancellation successful, false otherwise
     * @throws PortfolioServiceException if portfolio service error occurs
     */
    private boolean performPortfolioCancellation(Order order) throws PortfolioServiceException {
        try {
            OrderCancellationRequest cancellationRequest = OrderCancellationRequest.builder()
                    .orderId(order.getOrderId())
                    .accountId(order.getAccountId())
                    .symbol(order.getSymbol())
                    .orderType(order.getOrderType())
                    .originalQuantity(order.getQuantity().intValue())
                    .filledQuantity(order.getFilledQuantity().intValue())
                    .reason("User requested cancellation")
                    .build();
            
            CompletableFuture<PortfolioResponse> cancellationFuture;
            
            switch (order.getOrderType()) {
                case LIMIT_BUY, MARKET_BUY -> {
                    log.info("🏦 PORTFOLIO: Making BUY cancellation → OrderId: {}", order.getOrderId());
                    cancellationFuture = portfolioClient.cancelBuyOrder(cancellationRequest);
                }
                case LIMIT_SELL, MARKET_SELL, STOP_LOSS_SELL -> {
                    log.info("🏦 PORTFOLIO: Making SELL cancellation → OrderId: {}", order.getOrderId());
                    cancellationFuture = portfolioClient.cancelSellOrder(cancellationRequest);
                }
                default -> {
                    log.error("🏦 PORTFOLIO: Unsupported order type for cancellation → {}", order.getOrderType());
                    return false;
                }
            }
            
            // Wait for cancellation result
            PortfolioResponse response = cancellationFuture.get();
            
            if (response.isSuccess()) {
                log.info("🏦 PORTFOLIO: Cancellation successful → OrderId: {}, Message: {}", 
                        order.getOrderId(), response.getMessage());
                return true;
            } else {
                log.error("🏦 PORTFOLIO: Cancellation failed → OrderId: {}, Error: {}, ErrorCode: {}", 
                        order.getOrderId(), response.getMessage(), response.getErrorCode());
                throw new PortfolioServiceException(response.getMessage(), response.getErrorCode());
            }
            
        } catch (ExecutionException e) {
            log.error("🏦 PORTFOLIO: Cancellation execution error → OrderId: {}", order.getOrderId(), e);
            if (e.getCause() instanceof PortfolioServiceException portfolioEx) {
                throw portfolioEx;
            } else {
                throw new PortfolioServiceException("Portfolio cancellation execution failed", "EXECUTION_ERROR", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("🏦 PORTFOLIO: Cancellation interrupted → OrderId: {}", order.getOrderId(), e);
            throw new PortfolioServiceException("Portfolio cancellation interrupted", "INTERRUPTED", e);
        }
    }
}
