package com.stofina.orderservice.repository;

import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.enums.OrderSide;
import com.stofina.orderservice.enums.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByAccountIdAndStatus(Long accountId, OrderStatus status);

    List<Order> findBySymbolAndStatusIn(String symbol, List<OrderStatus> statuses);

    @Query("")
    List<Order> findActiveOrdersForMatching(String symbol, OrderSide side);

    @Query("")
    List<Order> findStopLossOrdersToCheck();

    Page<Order> findByTenantIdAndCreatedAtBetween(Long tenantId,
                                                  LocalDateTime start,
                                                  LocalDateTime end,
                                                  Pageable pageable);

    @Query("")
    List<Order> findExpiredOrders(LocalDateTime now);

    @Modifying
    @Transactional
    @Query("")
    int updateOrderStatus(Long orderId, OrderStatus newStatus);
}
