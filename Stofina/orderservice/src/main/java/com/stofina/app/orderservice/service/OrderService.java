package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.dto.request.CreateOrderRequest;
import com.stofina.app.orderservice.dto.request.OrderFilterRequest;
import com.stofina.app.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.app.orderservice.dto.response.OrderResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse updateOrder(Long orderId, UpdateOrderRequest request);

    void cancelOrder(Long orderId);

    OrderResponse getOrder(Long orderId);

    Page<OrderResponse> getOrders(OrderFilterRequest filter);

    List<OrderResponse> getActiveOrdersBySymbol(String symbol);

    List<OrderResponse> getOrdersByAccount(Long accountId);

    Map<String, Object> validateOrder(CreateOrderRequest request);

    int processExpiredOrders();

}
