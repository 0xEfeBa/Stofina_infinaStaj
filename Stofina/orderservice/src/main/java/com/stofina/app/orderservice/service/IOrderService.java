package com.stofina.app.orderservice.service;

import com.stofina.orderservice.common.ServiceResult;
import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.enums.OrderStatus;

public interface IOrderService {
    void executeMarketOrder(Order order);

    ServiceResult<Void> updateOrderStatus(Long orderId, OrderStatus newStatus);

    ServiceResult<Order> createOrder(Order order);
}
}