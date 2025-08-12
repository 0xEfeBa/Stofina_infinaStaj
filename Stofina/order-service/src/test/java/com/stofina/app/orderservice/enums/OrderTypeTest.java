package com.stofina.app.orderservice.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTypeTest {

    @Test
    void testRequiresPrice_ForLimitOrders() {
        assertTrue(OrderType.LIMIT_BUY.requiresPrice());
        assertTrue(OrderType.LIMIT_SELL.requiresPrice());
        assertTrue(OrderType.STOP_LOSS_SELL.requiresPrice());
    }

    @Test
    void testRequiresPrice_ForMarketOrders() {
        assertFalse(OrderType.MARKET_BUY.requiresPrice());
        assertFalse(OrderType.MARKET_SELL.requiresPrice());
    }

    @Test
    void testRequiresStopPrice_OnlyForStopLoss() {
        assertTrue(OrderType.STOP_LOSS_SELL.requiresStopPrice());
        assertFalse(OrderType.LIMIT_BUY.requiresStopPrice());
        assertFalse(OrderType.MARKET_BUY.requiresStopPrice());
    }

    @Test
    void testIsLimitOrder() {
        assertTrue(OrderType.LIMIT_BUY.isLimitOrder());
        assertTrue(OrderType.LIMIT_SELL.isLimitOrder());
        assertFalse(OrderType.MARKET_BUY.isLimitOrder());
        assertFalse(OrderType.STOP_LOSS_SELL.isLimitOrder());
    }

    @Test
    void testIsMarketOrder() {
        assertTrue(OrderType.MARKET_BUY.isMarketOrder());
        assertTrue(OrderType.MARKET_SELL.isMarketOrder());
        assertFalse(OrderType.LIMIT_BUY.isMarketOrder());
        assertFalse(OrderType.STOP_LOSS_SELL.isMarketOrder());
    }

    @Test
    void testIsStopLossOrder() {
        assertTrue(OrderType.STOP_LOSS_SELL.isStopLossOrder());
        assertFalse(OrderType.LIMIT_BUY.isStopLossOrder());
        assertFalse(OrderType.MARKET_BUY.isStopLossOrder());
    }

    @Test
    void testGetSide_ForBuyOrders() {
        assertEquals(OrderSide.BUY, OrderType.LIMIT_BUY.getSide());
        assertEquals(OrderSide.BUY, OrderType.MARKET_BUY.getSide());
    }

    @Test
    void testGetSide_ForSellOrders() {
        assertEquals(OrderSide.SELL, OrderType.LIMIT_SELL.getSide());
        assertEquals(OrderSide.SELL, OrderType.MARKET_SELL.getSide());
        assertEquals(OrderSide.SELL, OrderType.STOP_LOSS_SELL.getSide());
    }

    @Test
    void testIsBuyOrder() {
        assertTrue(OrderType.LIMIT_BUY.isBuyOrder());
        assertTrue(OrderType.MARKET_BUY.isBuyOrder());
        assertFalse(OrderType.LIMIT_SELL.isBuyOrder());
        assertFalse(OrderType.STOP_LOSS_SELL.isBuyOrder());
    }

    @Test
    void testIsSellOrder() {
        assertTrue(OrderType.LIMIT_SELL.isSellOrder());
        assertTrue(OrderType.MARKET_SELL.isSellOrder());
        assertTrue(OrderType.STOP_LOSS_SELL.isSellOrder());
        assertFalse(OrderType.LIMIT_BUY.isSellOrder());
    }

    @Test
    void testDisplayName() {
        assertEquals("Limit Alış", OrderType.LIMIT_BUY.getDisplayName());
        assertEquals("Market Satış", OrderType.MARKET_SELL.getDisplayName());
        assertEquals("Stop Loss Satış", OrderType.STOP_LOSS_SELL.getDisplayName());
    }

    @Test
    void testDescription() {
        assertNotNull(OrderType.LIMIT_BUY.getDescription());
        assertNotNull(OrderType.MARKET_SELL.getDescription());
        assertTrue(OrderType.LIMIT_BUY.getDescription().contains("alış"));
    }
}