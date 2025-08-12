package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.dto.request.CreateOrderRequest;
import com.stofina.app.orderservice.enums.OrderType;
import com.stofina.app.orderservice.service.client.MarketDataClient;
import com.stofina.app.orderservice.service.client.PortfolioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

    @Mock
    private MarketDataClient marketDataClient;

    @Mock
    private PortfolioClient portfolioClient;

    @InjectMocks
    private ValidationServiceImpl validationService;

    private CreateOrderRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CreateOrderRequest();
        validRequest.setSymbol("THYAO");
        validRequest.setOrderType(OrderType.LIMIT_BUY);
        validRequest.setQuantity(100);
        validRequest.setPrice(new BigDecimal("45.50"));
        validRequest.setAccountId(1L);
        validRequest.setTenantId(100L);
    }

    @Test
    void testValidateOrderRequest_WithValidLimitOrder() {
        assertDoesNotThrow(() -> validationService.validateOrderRequest(validRequest));
    }

    @Test
    void testValidateOrderRequest_WithValidMarketOrder() {
        validRequest.setOrderType(OrderType.MARKET_BUY);
        validRequest.setPrice(null);

        assertDoesNotThrow(() -> validationService.validateOrderRequest(validRequest));
    }

    @Test
    void testValidateOrderRequest_WithZeroQuantity() {
        validRequest.setQuantity(0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validationService.validateOrderRequest(validRequest)
        );

        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void testValidateOrderRequest_WithNegativeQuantity() {
        validRequest.setQuantity(-10);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validationService.validateOrderRequest(validRequest)
        );

        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void testValidateOrderRequest_LimitOrderWithoutPrice() {
        validRequest.setOrderType(OrderType.LIMIT_BUY);
        validRequest.setPrice(null);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validationService.validateOrderRequest(validRequest)
        );

        assertEquals("Price must be provided for this order type", exception.getMessage());
    }

    @Test
    void testValidateOrderRequest_LimitOrderWithZeroPrice() {
        validRequest.setOrderType(OrderType.LIMIT_BUY);
        validRequest.setPrice(BigDecimal.ZERO);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validationService.validateOrderRequest(validRequest)
        );

        assertEquals("Price must be provided for this order type", exception.getMessage());
    }

    @Test
    void testCheckMarketHours_DuringMarketHours() {
        LocalTime currentTime = LocalTime.now();
        
        if (currentTime.isAfter(LocalTime.of(9, 30)) && 
            currentTime.isBefore(LocalTime.of(18, 0))) {
            assertTrue(validationService.checkMarketHours());
        } else {
            assertFalse(validationService.checkMarketHours());
        }
    }

    @Test
    void testCheckPriceLimits_WithValidPrice() {
        BigDecimal currentPrice = new BigDecimal("45.00");
        BigDecimal orderPrice = new BigDecimal("45.50");

        when(marketDataClient.getCurrentPrice("THYAO")).thenReturn(currentPrice);
        when(marketDataClient.validatePriceInRange("THYAO", orderPrice)).thenReturn(true);

        assertDoesNotThrow(() -> validationService.checkPriceLimits("THYAO", orderPrice));
    }

    @Test
    void testCheckPriceLimits_WithInvalidPrice() {
        BigDecimal currentPrice = new BigDecimal("45.00");
        BigDecimal orderPrice = new BigDecimal("50.00");

        when(marketDataClient.getCurrentPrice("THYAO")).thenReturn(currentPrice);
        when(marketDataClient.validatePriceInRange("THYAO", orderPrice)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validationService.checkPriceLimits("THYAO", orderPrice)
        );

        assertTrue(exception.getMessage().contains("Fiyat ±%1.5 limit dışı"));
    }

    @Test
    void testCheckPriceLimits_WithNullMarketPrice() {
        BigDecimal orderPrice = new BigDecimal("45.50");

        when(marketDataClient.getCurrentPrice("THYAO")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validationService.checkPriceLimits("THYAO", orderPrice)
        );

        assertEquals("Market fiyatı alınamadı: THYAO", exception.getMessage());
    }
}