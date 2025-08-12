package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.common.ServiceResult;
import com.stofina.app.orderservice.entity.Order;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for managing stop-loss orders.
 * Provides functionality for creating and monitoring stop-loss orders.
 */
public interface IStopLossService {

    /**
     * Adds a new stop-loss order to be monitored.
     *
     * @param order the stop-loss order to add
     * @return ServiceResult indicating success or failure
     */
    ServiceResult<Void> addStopLossOrder(Order order);

    /**
     * Checks if any stop-loss orders should be triggered for the given price.
     *
     * @param symbol the stock symbol to check
     * @param currentPrice the current price to check against
     * @return ServiceResult containing list of triggered orders
     */
    ServiceResult<List<Order>> checkPrice(String symbol, BigDecimal currentPrice);

    /**
     * Removes a stop-loss order from monitoring.
     *
     * @param orderId the ID of the order to remove
     * @return ServiceResult indicating if removal was successful
     */
    ServiceResult<Boolean> remove(Long orderId);

    /**
     * Checks if an order is currently being monitored for stop-loss.
     *
     * @param orderId the ID of the order to check
     * @return ServiceResult indicating if the order is being watched
     */
    ServiceResult<Boolean> isWatching(Long orderId);

    /**
     * Retrieves all orders currently being monitored for stop-loss.
     *
     * @return ServiceResult containing list of all watched orders
     */
    ServiceResult<List<Order>> getAllWatched();

}
