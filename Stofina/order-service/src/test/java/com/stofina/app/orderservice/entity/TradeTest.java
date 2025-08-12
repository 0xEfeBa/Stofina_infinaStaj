package com.stofina.app.orderservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TradeTest {

    private Trade trade;

    @BeforeEach
    void setUp() {
        trade = new Trade();
        trade.setTradeId(1L);
        trade.setTenantId(100L);
        trade.setSymbol("THYAO");
        trade.setBuyOrderId(1L);
        trade.setSellOrderId(2L);
        trade.setBuyAccountId(100L);
        trade.setSellAccountId(200L);
        trade.setPrice(new BigDecimal("45.50"));
        trade.setQuantity(new BigDecimal("100"));
    }

    @Test
    void testGetTradeAmount_WithValidValues() {
        trade.setPrice(new BigDecimal("45.50"));
        trade.setQuantity(new BigDecimal("100"));

        BigDecimal amount = trade.getTradeAmount();

        assertEquals(new BigDecimal("4550.00"), amount);
    }

    @Test
    void testGetTradeAmount_WithNullPrice() {
        trade.setPrice(null);
        trade.setQuantity(new BigDecimal("100"));

        BigDecimal amount = trade.getTradeAmount();

        assertEquals(BigDecimal.ZERO, amount);
    }

    @Test
    void testGetTradeAmount_WithNullQuantity() {
        trade.setPrice(new BigDecimal("45.50"));
        trade.setQuantity(null);

        BigDecimal amount = trade.getTradeAmount();

        assertEquals(BigDecimal.ZERO, amount);
    }

    @Test
    void testGenerateTradeRef_WithSymbol() {
        trade.setSymbol("THYAO");

        String tradeRef = trade.generateTradeRef();

        assertNotNull(tradeRef);
        assertTrue(tradeRef.startsWith("TRD_THYAO_"));
        assertEquals(18, tradeRef.length());
    }

    @Test
    void testGenerateTradeRef_WithNullSymbol() {
        trade.setSymbol(null);

        String tradeRef = trade.generateTradeRef();

        assertNotNull(tradeRef);
        assertTrue(tradeRef.startsWith("TRD_UNK_"));
        assertEquals(16, tradeRef.length());
    }

    @Test
    void testGenerateTradeRef_IsUnique() {
        trade.setSymbol("THYAO");

        String tradeRef1 = trade.generateTradeRef();
        String tradeRef2 = trade.generateTradeRef();

        assertNotEquals(tradeRef1, tradeRef2);
    }

    @Test
    void testTradeRefFormat() {
        trade.setSymbol("GARAN");

        String tradeRef = trade.generateTradeRef();

        assertTrue(tradeRef.matches("^TRD_[A-Z]+_[A-Z0-9]{8}$"));
    }
}