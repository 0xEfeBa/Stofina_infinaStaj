package com.stofina.app.orderservice.entity;

import com.stofina.app.orderservice.enums.OrderSide;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderId(1L);
        order.setTenantId(100L);
        order.setAccountId(200L);
        order.setSymbol("THYAO");
        order.setOrderType(OrderType.LIMIT_BUY);
        order.setSide(OrderSide.BUY);
        order.setQuantity(new BigDecimal("100"));
        order.setPrice(new BigDecimal("45.50"));
        order.setFilledQuantity(BigDecimal.ZERO);
        order.setStatus(OrderStatus.NEW);
    }

    @Test
    void testGetRemainingQuantity_WhenOrderIsNew() {
        order.setQuantity(new BigDecimal("100"));
        order.setFilledQuantity(BigDecimal.ZERO);

        BigDecimal remaining = order.getRemainingQuantity();

        assertEquals(new BigDecimal("100"), remaining);
    }

    @Test
    void testGetRemainingQuantity_WhenPartiallyFilled() {
        order.setQuantity(new BigDecimal("100"));
        order.setFilledQuantity(new BigDecimal("30"));

        BigDecimal remaining = order.getRemainingQuantity();

        assertEquals(new BigDecimal("70"), remaining);
    }

    @Test
    void testGetRemainingQuantity_WhenNullValues() {
        order.setQuantity(null);
        order.setFilledQuantity(null);

        BigDecimal remaining = order.getRemainingQuantity();

        assertEquals(BigDecimal.ZERO, remaining);
    }

    @Test
    void testIsFullyFilled_WhenCompletelyFilled() {
        order.setQuantity(new BigDecimal("100"));
        order.setFilledQuantity(new BigDecimal("100"));

        boolean isFullyFilled = order.isFullyFilled();

        assertTrue(isFullyFilled);
    }

    @Test
    void testIsFullyFilled_WhenPartiallyFilled() {
        order.setQuantity(new BigDecimal("100"));
        order.setFilledQuantity(new BigDecimal("50"));

        boolean isFullyFilled = order.isFullyFilled();

        assertFalse(isFullyFilled);
    }

    @Test
    void testIsFullyFilled_WhenNullValues() {
        order.setQuantity(null);
        order.setFilledQuantity(null);

        boolean isFullyFilled = order.isFullyFilled();

        assertFalse(isFullyFilled);
    }

    @Test
    void testCanBeCancelled_WhenStatusIsNew() {
        order.setStatus(OrderStatus.NEW);

        boolean canCancel = order.canBeCancelled();

        assertTrue(canCancel);
    }

    @Test
    void testCanBeCancelled_WhenStatusIsActive() {
        order.setStatus(OrderStatus.ACTIVE);

        boolean canCancel = order.canBeCancelled();

        assertTrue(canCancel);
    }

    @Test
    void testCanBeCancelled_WhenStatusIsFilled() {
        order.setStatus(OrderStatus.FILLED);

        boolean canCancel = order.canBeCancelled();

        assertFalse(canCancel);
    }

    @Test
    void testCanBeUpdated_WhenStatusIsActive() {
        order.setStatus(OrderStatus.ACTIVE);

        boolean canUpdate = order.canBeUpdated();

        assertTrue(canUpdate);
    }

    @Test
    void testCanBeUpdated_WhenStatusIsFilled() {
        order.setStatus(OrderStatus.FILLED);

        boolean canUpdate = order.canBeUpdated();

        assertFalse(canUpdate);
    }
}