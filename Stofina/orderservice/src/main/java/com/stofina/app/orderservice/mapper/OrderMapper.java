package com.stofina.app.orderservice.mapper;

import com.stofina.orderservice.dto.request.CreateOrderRequest;
import com.stofina.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.orderservice.dto.response.OrderResponse;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    /**
     * CreateOrderRequest -> Order entity dönüşümü
     */
    public static Order toEntity(CreateOrderRequest request) {
        Order order = new Order();
        order.setAccountId(request.getAccountId());
        order.setSymbol(request.getSymbol());
        order.setOrderType(request.getOrderType());
        order.setSide(request.getSide());
        order.setQuantity(request.getQuantity());
        order.setPrice(request.getPrice());
        order.setStopPrice(request.getStopPrice());
        order.setTimeInForce(request.getTimeInForce());
        order.setExpiryDate(request.getExpiryDate());
        order.setClientOrderId(request.getClientOrderId());
        // Status, filledQuantity, remainingQuantity, createdAt vb. burada set edilebilir
        return order;
    }

    /**
     * Order entity -> OrderResponse dönüşümü
     */
    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .accountId(order.getAccountId())
                .symbol(order.getSymbol())
                .orderType(order.getOrderType())
                .side(order.getSide())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .filledQuantity(order.getFilledQuantity())
                .remainingQuantity(order.getRemainingQuantity())
                .averagePrice(order.getAveragePrice())
                .status(order.getStatus())
                .timeInForce(order.getTimeInForce())
                .stopPrice(order.getStopPrice())
                .expiryDate(order.getExpiryDate())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .trades(order.getTrades())  // trades için ayrı bir mapper gerekebilir
                .build();
    }

    /**
     * List<Order> -> List<OrderResponse> dönüşümü
     */
    public static List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * UpdateOrderRequest -> var olan Order entity güncellemesi
     */
    public static void updateEntity(Order existing, UpdateOrderRequest request) {
        if (request.getPrice() != null) {
            existing.setPrice(request.getPrice());
        }
        if (request.getQuantity() != null) {
            existing.setQuantity(request.getQuantity());
        }
        existing.setUpdatedAt(java.time.LocalDateTime.now());
    }
}
