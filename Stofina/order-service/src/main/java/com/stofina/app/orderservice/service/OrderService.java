package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.dto.request.CreateOrderRequest;
import com.stofina.app.orderservice.dto.request.OrderFilterRequest;
import com.stofina.app.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.app.orderservice.dto.response.OrderResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing orders in the trading system.
 * Provides methods for creating, updating, canceling, and querying orders.
 */
public interface OrderService {

    /**
     * Creates a new order in the system.
     *
     * @param request the order creation request
     * @return OrderResponse containing the created order details
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Updates an existing order.
     *
     * @param orderId the ID of the order to update
     * @param request the order update request
     * @return OrderResponse containing the updated order details
     */
    OrderResponse updateOrder(Long orderId, UpdateOrderRequest request);

    /**
     * Cancels an existing order.
     *
     * @param orderId the ID of the order to cancel
     */
    void cancelOrder(Long orderId);

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId the ID of the order to retrieve
     * @return OrderResponse containing the order details
     */
    OrderResponse getOrder(Long orderId);

    /**
     * Retrieves a paginated list of orders based on filter criteria.
     *
     * @param filter the filter criteria for orders
     * @return Page of OrderResponse matching the filter criteria
     */
    Page<OrderResponse> getOrders(OrderFilterRequest filter);

    /**
     * Retrieves all active orders for a specific symbol.
     *
     * @param symbol the stock symbol to query
     * @return List of active orders for the symbol
     */
    List<OrderResponse> getActiveOrdersBySymbol(String symbol);

    /**
     * Retrieves all orders for a specific account.
     *
     * @param accountId the account ID to query
     * @return List of orders for the account
     */
    List<OrderResponse> getOrdersByAccount(Long accountId);

    /**
     * Validates a new order request before creation.
     *
     * @param request the order request to validate
     * @return Map containing validation results
     */
    Map<String, Object> validateOrder(CreateOrderRequest request);

    /**
     * Processes and removes expired orders from the system.
     *
     * @return number of expired orders processed
     */
    int processExpiredOrders();

}
