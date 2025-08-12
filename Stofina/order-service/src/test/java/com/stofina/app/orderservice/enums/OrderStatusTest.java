package com.stofina.app.orderservice.enums;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void testIsFinal_ForFinalStatuses() {
        assertTrue(OrderStatus.FILLED.isFinal());
        assertTrue(OrderStatus.CANCELLED.isFinal());
        assertTrue(OrderStatus.REJECTED.isFinal());
        assertTrue(OrderStatus.EXPIRED.isFinal());
    }

    @Test
    void testIsFinal_ForNonFinalStatuses() {
        assertFalse(OrderStatus.NEW.isFinal());
        assertFalse(OrderStatus.PENDING_TRIGGER.isFinal());
        assertFalse(OrderStatus.ACTIVE.isFinal());
        assertFalse(OrderStatus.PARTIALLY_FILLED.isFinal());
    }

    @Test
    void testCanUpdate_ForActiveOrders() {
        assertTrue(OrderStatus.ACTIVE.canUpdate());
        assertTrue(OrderStatus.PARTIALLY_FILLED.canUpdate());
    }

    @Test
    void testCanUpdate_ForNonActiveOrders() {
        assertFalse(OrderStatus.NEW.canUpdate());
        assertFalse(OrderStatus.FILLED.canUpdate());
        assertFalse(OrderStatus.CANCELLED.canUpdate());
        assertFalse(OrderStatus.REJECTED.canUpdate());
    }

    @Test
    void testCanCancel_ForCancellableOrders() {
        assertTrue(OrderStatus.NEW.canCancel());
        assertTrue(OrderStatus.ACTIVE.canCancel());
        assertTrue(OrderStatus.PARTIALLY_FILLED.canCancel());
        assertTrue(OrderStatus.PENDING_TRIGGER.canCancel());
    }

    @Test
    void testCanCancel_ForNonCancellableOrders() {
        assertFalse(OrderStatus.FILLED.canCancel());
        assertFalse(OrderStatus.CANCELLED.canCancel());
        assertFalse(OrderStatus.REJECTED.canCancel());
        assertFalse(OrderStatus.EXPIRED.canCancel());
    }

    @Test
    void testIsActive_ForActiveStatuses() {
        assertTrue(OrderStatus.ACTIVE.isActive());
        assertTrue(OrderStatus.PARTIALLY_FILLED.isActive());
    }

    @Test
    void testIsActive_ForNonActiveStatuses() {
        assertFalse(OrderStatus.NEW.isActive());
        assertFalse(OrderStatus.FILLED.isActive());
        assertFalse(OrderStatus.CANCELLED.isActive());
    }

    @Test
    void testGetAllowedTransitions_FromNew() {
        Set<OrderStatus> transitions = OrderStatus.NEW.getAllowedTransitions();
        
        assertEquals(4, transitions.size());
        assertTrue(transitions.contains(OrderStatus.ACTIVE));
        assertTrue(transitions.contains(OrderStatus.REJECTED));
        assertTrue(transitions.contains(OrderStatus.CANCELLED));
        assertTrue(transitions.contains(OrderStatus.PENDING_TRIGGER));
    }

    @Test
    void testGetAllowedTransitions_FromActive() {
        Set<OrderStatus> transitions = OrderStatus.ACTIVE.getAllowedTransitions();
        
        assertEquals(4, transitions.size());
        assertTrue(transitions.contains(OrderStatus.PARTIALLY_FILLED));
        assertTrue(transitions.contains(OrderStatus.FILLED));
        assertTrue(transitions.contains(OrderStatus.CANCELLED));
        assertTrue(transitions.contains(OrderStatus.EXPIRED));
    }

    @Test
    void testGetAllowedTransitions_FromFinalStates() {
        assertTrue(OrderStatus.FILLED.getAllowedTransitions().isEmpty());
        assertTrue(OrderStatus.CANCELLED.getAllowedTransitions().isEmpty());
        assertTrue(OrderStatus.REJECTED.getAllowedTransitions().isEmpty());
        assertTrue(OrderStatus.EXPIRED.getAllowedTransitions().isEmpty());
    }

    @Test
    void testDisplayName() {
        assertEquals("Yeni", OrderStatus.NEW.getDisplayName());
        assertEquals("Aktif", OrderStatus.ACTIVE.getDisplayName());
        assertEquals("Tamamen Gerçekleşti", OrderStatus.FILLED.getDisplayName());
        assertEquals("İptal Edildi", OrderStatus.CANCELLED.getDisplayName());
    }
}