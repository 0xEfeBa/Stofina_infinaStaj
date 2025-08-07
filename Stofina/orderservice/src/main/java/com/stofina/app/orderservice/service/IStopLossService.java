package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.common.ServiceResult;
import com.stofina.app.orderservice.entity.Order;

import java.math.BigDecimal;
import java.util.List;

public interface IStopLossService {

    ServiceResult<Void> addStopLossOrder(Order order);

    ServiceResult<List<Order>> checkPrice(String symbol, BigDecimal currentPrice);

    ServiceResult<Boolean> remove(Long orderId);

    ServiceResult<Boolean> isWatching(Long orderId);

    ServiceResult<List<Order>> getAllWatched();

}
