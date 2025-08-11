package com.stofina.app.orderservice.controller;

import com.stofina.app.orderservice.common.ServiceResult;
import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.service.IStopLossService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static com.stofina.app.orderservice.constants.OrderConstants.StopLoss.*;

@RestController
@RequestMapping(BASE_PATH)
@RequiredArgsConstructor
public class StopLossController {

    private final IStopLossService stopLossService;

    @PostMapping(ADD)
    public ResponseEntity<ServiceResult<Void>> addStopLoss(@RequestBody Order order) {
        ServiceResult<Void> result = stopLossService.addStopLossOrder(order);
        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @PostMapping(CHECK)
    public ResponseEntity<ServiceResult<List<Order>>> checkStopLossTriggers (@RequestParam("symbol") String symbol, @RequestParam("price") BigDecimal price
    ) {
        ServiceResult<List<Order>> result = stopLossService.checkPrice(symbol, price);
        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @GetMapping(ALL)
    public ResponseEntity<ServiceResult<List<Order>>> getAllWatchedOrders() {
        ServiceResult<List<Order>> result = stopLossService.getAllWatched();
        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @GetMapping(IS_WATCHING)
    public ResponseEntity<ServiceResult<Boolean>> isWatching(@PathVariable("orderId") Long orderId) {
        ServiceResult<Boolean> result = stopLossService.isWatching(orderId);
        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }

    @DeleteMapping(REMOVE)
    public ResponseEntity<ServiceResult<Boolean>> removeWatcher(@PathVariable("orderId") Long orderId) {
        ServiceResult<Boolean> result = stopLossService.remove(orderId);
        return ResponseEntity.status(result.getHttpStatus()).body(result);
    }



}
