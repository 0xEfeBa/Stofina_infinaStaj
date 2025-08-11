package com.stofina.app.orderservice.controller;

import com.stofina.app.orderservice.constants.ApiEndpoints;

import com.stofina.app.orderservice.dto.request.CreateOrderRequest;
import com.stofina.app.orderservice.dto.request.OrderFilterRequest;
import com.stofina.app.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.app.orderservice.dto.response.OrderResponse;
import com.stofina.app.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping(ApiEndpoints.ORDERS_BASE)
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request){
        log.info("🚀 LIFECYCLE-1: OrderController.createOrder() - ENTRY - symbol={}, orderType={}, quantity={}, price={}", 
                 request.getSymbol(), request.getOrderType(), request.getQuantity(), request.getPrice());
        OrderResponse response = orderService.createOrder(request);
        log.info("🚀 LIFECYCLE-1: OrderController.createOrder() - EXIT - orderId={}, status={}", 
                 response.getOrderId(), response.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping(ApiEndpoints.ORDER_BY_ID)
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PatchMapping(ApiEndpoints.ORDER_BY_ID)
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long orderId,
                                                     @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, request));
    }

    @DeleteMapping(ApiEndpoints.ORDER_BY_ID)
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(@ModelAttribute OrderFilterRequest filter) {
        return ResponseEntity.ok(orderService.getOrders(filter));
    }

    @GetMapping(ApiEndpoints.ORDERS_BY_ACCOUNT)
    public ResponseEntity<List<OrderResponse>> getOrdersByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(orderService.getOrdersByAccount(accountId));
    }

    @GetMapping(ApiEndpoints.ORDERS_BY_SYMBOL)
    public ResponseEntity<List<OrderResponse>> getOrdersBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(orderService.getActiveOrdersBySymbol(symbol));
    }

    @PostMapping(ApiEndpoints.ORDERS_VALIDATE)
    public ResponseEntity<Map<String, Object>> validateOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.validateOrder(request));
    }
}  