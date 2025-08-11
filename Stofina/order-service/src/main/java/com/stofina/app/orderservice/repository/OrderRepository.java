package com.stofina.app.orderservice.repository;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.enums.OrderSide;
import com.stofina.app.orderservice.enums.OrderStatus;
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

    @Query("SELECT o FROM Order o WHERE o.symbol = :symbol AND o.side = :side AND o.status IN ('ACTIVE', 'PARTIALLY_FILLED') ORDER BY o.price ASC, o.createdAt ASC")
    List<Order> findActiveOrdersForMatching(String symbol, OrderSide side);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING_TRIGGER' AND o.stopPrice IS NOT NULL")
    List<Order> findStopLossOrdersToCheck();

    Page<Order> findByTenantIdAndCreatedAtBetween(Long tenantId,
                                                  LocalDateTime start,
                                                  LocalDateTime end,
                                                  Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.expiryDate < :now AND o.status IN ('NEW', 'ACTIVE', 'PARTIALLY_FILLED', 'PENDING_TRIGGER')")
    List<Order> findExpiredOrders(LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :newStatus, o.updatedAt = CURRENT_TIMESTAMP WHERE o.orderId = :orderId")
    int updateOrderStatus(Long orderId, OrderStatus newStatus);

    // Filter by accountId with pagination
    Page<Order> findByAccountId(Long accountId, Pageable pageable);
    
    // For integration tests - findByAccountId without pagination
    List<Order> findByAccountId(Long accountId);
    
    // For integration tests - findBySymbolAndStatus
    List<Order> findBySymbolAndStatus(String symbol, OrderStatus status);
    
    // For integration tests - findActiveOrdersBySymbol
    @Query("SELECT o FROM Order o WHERE o.symbol = :symbol AND o.status IN ('ACTIVE', 'NEW') ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersBySymbol(String symbol);
}
