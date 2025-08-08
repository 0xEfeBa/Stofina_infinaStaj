package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.common.ServiceResult;
import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.enums.OrderStatus;

public interface IOrderService {
    void executeMarketOrder(Order order);
    ServiceResult<Void> updateOrderStatus(Long orderId, OrderStatus newStatus);
    ServiceResult<Order> createOrder(Order order);
}
