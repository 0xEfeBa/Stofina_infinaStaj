package com.stofina.app.orderservice.mapper;

import com.stofina.app.orderservice.dto.request.CreateOrderRequest;
import com.stofina.app.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.app.orderservice.dto.response.OrderResponse;
import com.stofina.app.orderservice.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    /**
     * CreateOrderRequest -> Order entity dönüşümü
     */
    public Order toEntity(CreateOrderRequest request) {
        Order order = new Order();
        order.setTenantId(request.getTenantId()); // DÜZELTME: tenantId mapping eklendi
        order.setAccountId(request.getAccountId());
        order.setSymbol(request.getSymbol());
        order.setOrderType(request.getOrderType());
        order.setSide(request.getOrderType().getSide());
        order.setQuantity(java.math.BigDecimal.valueOf(request.getQuantity()));
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
    public OrderResponse toResponse(Order order) {
        return OrderResponse.fromEntity(order);
    }

    /**
     * List<Order> -> List<OrderResponse> dönüşümü
     */
    public List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * UpdateOrderRequest -> var olan Order entity güncellemesi
     */
    public void updateEntity(Order existing, UpdateOrderRequest request) {
        if (request.getPrice() != null) {
            existing.setPrice(request.getPrice());
        }
        if (request.getQuantity() != null) {
            existing.setQuantity(request.getQuantity());
        }
        existing.setUpdatedAt(java.time.LocalDateTime.now());
    }
}
