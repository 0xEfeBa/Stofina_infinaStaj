"use client";

import React, { useState, useMemo, useCallback, useEffect } from "react";
import { useSearchParams } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import styles from "./Trading.module.css";
import { useDashboardContext } from "../../../../contexts/DashboardContext";

// Import WebSocket hooks
import { useMarketDataStreamSTOMP } from "../../../../hooks/websocket/useMarketDataStreamSTOMP";
import { useOrderBookStreamSTOMP } from "../../../../hooks/websocket/useOrderBookStreamSTOMP";
import { useTradeStreamSTOMP } from "../../../../hooks/websocket/useTradeStreamSTOMP";

// Import components
import { PriceDisplay } from "./components/PriceDisplay";
import { OrderBookTable } from "./components/OrderBookTable";
import { ConnectionStatus } from "./components/ConnectionStatus";

// Import types
import { WebSocketStatus } from "../../../../types/websocket.types";

// Import utilities
import { 
  calculateOrderTotalValue, 
  InvalidQuantityError, 
  InvalidPriceError 
} from "../../../../utils/tradingCalculations";
import { formatPriceToTurkishLira } from "../../../../utils/priceFormatters";

// Import order submission hook
import { useOrderSubmission } from "../../../../hooks/useOrderSubmission";

// Import market data API hook
import { useMarketDataAPI } from "../../../../hooks/useMarketDataAPI";

// Import types
import { OrderFormData } from "../../../../types/order.types";

// Import scheduled order components and hooks
import { ScheduledOrderSection } from "../../../../components/ScheduledOrderSection";
import { useScheduledOrder } from "../../../../hooks/useScheduledOrder";
import { formatScheduledTimeForAPI, validateScheduledOrderForm } from "../../../../utils/scheduledOrderUtils";

/**
 * Internal form state interface (different from API OrderFormData)
 */
interface InternalOrderFormData {
  orderType: string;
  priceType: string;
  limitPrice: string;
  quantity: string;
  isScheduled: boolean;
  scheduledTime?: Date;
}

/**
 * Available symbols for trading - Now using dynamic data from Market Data Service
 * Removed hardcoded symbols array
 */

/**
 * TradingPage Component
 * Single Responsibility: Orchestrate trading interface components
 * Clean Code: Small, focused, well-named functions
 */
export default function TradingPage() {
  // ================================
  // STATE MANAGEMENT - Clean, Minimal
  // ================================
  
  // i18n hook for translations
  const { t } = useTranslation();
  
  // Get URL parameters
  const searchParams = useSearchParams();
  const symbolParam = searchParams.get('symbol');
  const actionParam = searchParams.get('action');
  
  // Current selected symbol for trading (starts with THYAO, will be updated from market data)
  const [currentTradingSymbol, setCurrentTradingSymbol] = useState<string>(symbolParam || "THYAO");
  
  // Order form state - Initialize with URL parameters
  const [orderFormData, setOrderFormData] = useState<InternalOrderFormData>({
    orderType: actionParam === 'sell' ? "sell" : "buy",
    priceType: "market",
    quantity: "",
    limitPrice: "",
    isScheduled: false,
    scheduledTime: undefined
  });

  // Order submission hook
  const { isSubmitting, submitOrder, lastSubmissionError, clearError } = useOrderSubmission();
  
  // Market data API hook (for initial data loading)
  const { symbols: apiSymbols, isLoading: isLoadingSymbols, error: apiError } = useMarketDataAPI();
  
  // UI state
  const [customerSearchQuery, setCustomerSearchQuery] = useState<string>("");

  // ================================
  // REAL-TIME DATA HOOKS
  // ================================
  
  // Dashboard context for user info
  const { dashboardData } = useDashboardContext();
  
  // Market data stream (prices) - Using STOMP protocol
  const marketDataStream = useMarketDataStreamSTOMP();
  
  // Order book stream (buy/sell orders) - Using STOMP protocol
  const orderBookStream = useOrderBookStreamSTOMP(currentTradingSymbol);
  
  // Trade stream (executed trades) - Using STOMP protocol
  const tradeStream = useTradeStreamSTOMP(currentTradingSymbol);

  // ================================
  // DERIVED STATE - Memoized
  // ================================
  
  /**
   * Get available symbols from both API and WebSocket data
   * Prioritize WebSocket data when available, fallback to API data
   */
  const availableSymbols = useMemo(() => {
    // Try to get data from WebSocket first
    const webSocketPrices = marketDataStream.getAllStockPrices();
    
    if (webSocketPrices.length > 0) {
      return webSocketPrices.map(stock => ({
        symbol: stock.symbol,
        name: stock.companyName || stock.symbol
      })).sort((a, b) => a.symbol.localeCompare(b.symbol));
    }
    
    // Fallback to API data if WebSocket data is not available
    if (apiSymbols.length > 0) {
      return apiSymbols.map(stock => ({
        symbol: stock.symbol,
        name: stock.companyName || stock.symbol
      })).sort((a, b) => a.symbol.localeCompare(b.symbol));
    }
    
    return [];
  }, [marketDataStream.stockPrices, apiSymbols]);
  
  /**
   * Get current stock price data from WebSocket or API
   */
  const currentStockPrice = useMemo(() => {
    if (!currentTradingSymbol) return null;
    
    // Try WebSocket data first
    const webSocketPrice = marketDataStream.getStockPrice(currentTradingSymbol);
    if (webSocketPrice) {
      return webSocketPrice;
    }
    
    // Fallback to API data
    const apiStock = apiSymbols.find(stock => stock.symbol === currentTradingSymbol);
    if (apiStock) {
      return {
        symbol: apiStock.symbol,
        price: apiStock.currentPrice,
        companyName: apiStock.companyName,
        change: apiStock.change,
        changePercent: (apiStock.change / (apiStock.currentPrice - apiStock.change)) * 100,
        volume: 0, // Not available from API
        lastUpdated: new Date(apiStock.lastUpdated)
      };
    }
    
    return null;
  }, [marketDataStream.stockPrices, currentTradingSymbol, apiSymbols]);


  const scheduledOrderValidation = useMemo(() => {
    return validateScheduledOrderForm(
      orderFormData.isScheduled,
      orderFormData.scheduledTime,
      currentTradingSymbol,
      parseFloat(orderFormData.quantity) || 0
    );
  }, [orderFormData.isScheduled, orderFormData.scheduledTime, currentTradingSymbol, orderFormData.quantity]);

  /**
   * Calculate order total value
   */
  const calculatedOrderTotal = useMemo(() => {
    try {
      const quantity = parseFloat(orderFormData.quantity) || 0;
      let price = 0;

      if (orderFormData.priceType === "market") {
        price = currentStockPrice?.price || 0;
      } else {
        price = parseFloat(orderFormData.limitPrice) || 0;
      }

      if (quantity <= 0 || price <= 0) return 0;
      
      return calculateOrderTotalValue(quantity, price);
    } catch (error) {
      if (error instanceof InvalidQuantityError || error instanceof InvalidPriceError) {
        return 0;
      }
      console.error('Order total calculation error:', error);
      return 0;
    }
  }, [orderFormData.quantity, orderFormData.limitPrice, orderFormData.priceType, currentStockPrice?.price]);

  // ================================
  // EVENT HANDLERS - Pure Functions
  // ================================
  
  /**
   * Handle symbol selection change
   */
  const handleSymbolChange = useCallback((newSymbol: string) => {
    if (newSymbol !== currentTradingSymbol) {
      setCurrentTradingSymbol(newSymbol);
      // Reset form when changing symbols
      setOrderFormData(prev => ({
        ...prev,
        quantity: "",
        limitPrice: ""
      }));
    }
  }, [currentTradingSymbol]);

  /**
   * Handle quantity input change
   */
  const handleQuantityChange = useCallback((value: string) => {
    // Validate numeric input
    if (value === "" || /^\d*\.?\d*$/.test(value)) {
      setOrderFormData(prev => ({ ...prev, quantity: value }));
    }
  }, []);

  /**
   * Handle limit price input change  
   */
  const handleLimitPriceChange = useCallback((value: string) => {
    // Validate numeric input
    if (value === "" || /^\d*\.?\d*$/.test(value)) {
      setOrderFormData(prev => ({ ...prev, limitPrice: value }));
    }
  }, []);

  /**
   * Validate order form data
   */
  const validateOrderForm = useCallback((): { isValid: boolean; errorMessage?: string } => {
    // Symbol validation
    if (!currentTradingSymbol || currentTradingSymbol.trim() === "") {
      return { isValid: false, errorMessage: t('trading.messages.invalidSymbol') };
    }

    // Quantity validation
    const quantity = parseFloat(orderFormData.quantity);
    if (!quantity || quantity <= 0) {
      return { isValid: false, errorMessage: t('trading.messages.invalidQuantity') };
    }

    // Price validation for non-market orders
    if (orderFormData.priceType !== "market") {
      const price = parseFloat(orderFormData.limitPrice);
      if (!price || price <= 0) {
        return { isValid: false, errorMessage: t('trading.messages.invalidPrice') };
      }
    }

    // Market price validation
    if (!currentStockPrice && orderFormData.priceType === "market") {
      return { isValid: false, errorMessage: t('trading.messages.marketPriceError') };
    }

    // Scheduled order validation
    if (orderFormData.isScheduled) {
      if (!scheduledOrderValidation.isValid) {
        const firstError = scheduledOrderValidation.errors[0];
        return { isValid: false, errorMessage: firstError?.message || t('scheduledOrder.validation.timeRequired') };
      }
    }

    return { isValid: true };
  }, [orderFormData, currentStockPrice, scheduledOrderValidation, currentTradingSymbol]);

  /**
   * Check if form is ready for submission
   */
  const isFormValid = useMemo(() => {
    return validateOrderForm().isValid;
  }, [validateOrderForm]);

  /**
   * Handle order submission
   */
  const handleOrderSubmission = useCallback(async (event: React.FormEvent) => {
    event.preventDefault();
    
    // Clear previous errors
    clearError();

    // Validate form
    const validation = validateOrderForm();
    if (!validation.isValid) {
      alert(validation.errorMessage);
      return;
    }

    try {
      // Create order data
      // Map frontend price types to backend order types
      let backendOrderType: string;
      if (orderFormData.priceType === "market") {
        backendOrderType = orderFormData.orderType === "buy" ? "MARKET_BUY" : "MARKET_SELL";
      } else if (orderFormData.priceType === "limit") {
        backendOrderType = orderFormData.orderType === "buy" ? "LIMIT_BUY" : "LIMIT_SELL";
      } else if (orderFormData.priceType === "stop" && orderFormData.orderType === "sell") {
        backendOrderType = "STOP_LOSS_SELL";
      } else {
        // This shouldn't happen due to UI restrictions
        throw new Error("Invalid order type combination");
      }
      
      const orderData: OrderFormData = {
        symbol: currentTradingSymbol,
        orderType: backendOrderType as any, // We'll need to update the type definition
        side: orderFormData.orderType.toUpperCase() as 'BUY' | 'SELL',
        quantity: parseFloat(orderFormData.quantity),
        price: orderFormData.priceType === "limit" ? parseFloat(orderFormData.limitPrice) : undefined,
        stopPrice: orderFormData.priceType === "stop" ? parseFloat(orderFormData.limitPrice) : undefined,
        isScheduled: orderFormData.isScheduled,
        scheduledTime: orderFormData.isScheduled && orderFormData.scheduledTime 
          ? formatScheduledTimeForAPI(orderFormData.scheduledTime) 
          : undefined
      };

      // Submit order to backend
      const result = await submitOrder(orderData);

      if (result.success) {
        // Show success message
        const orderTypeText = orderFormData.orderType === "buy" ? t('trading.orderTypes.buy') : t('trading.orderTypes.sell');
        alert(`${t('trading.messages.success', { type: orderTypeText })}\n${t('trading.messages.orderId', { id: result.data?.orderId })}`);
        
        // Reset form
        setOrderFormData(prev => ({
          ...prev,
          quantity: "",
          limitPrice: "",
          isScheduled: false,
          scheduledTime: undefined
        }));
      } else {
        // Error handled by useOrderSubmission hook
        alert(lastSubmissionError || t('trading.messages.unexpectedError'));
      }

    } catch (error) {
      console.error('Order submission error:', error);
      alert(t('trading.messages.unexpectedError'));
    }
  }, [orderFormData, currentTradingSymbol, validateOrderForm, submitOrder, clearError, lastSubmissionError]);

  // ================================
  // EFFECTS
  // ================================

  /**
   * Auto-select first available symbol when symbols are loaded
   * Only change if current symbol is not in the available list
   */
  useEffect(() => {
    if (availableSymbols.length > 0) {
      const currentSymbolExists = availableSymbols.some(stock => stock.symbol === currentTradingSymbol);
      if (!currentSymbolExists) {
        setCurrentTradingSymbol(availableSymbols[0].symbol);
      }
    }
  }, [availableSymbols, currentTradingSymbol]);

  /**
   * Update form when URL parameters change
   */
  useEffect(() => {
    if (symbolParam && symbolParam !== currentTradingSymbol) {
      setCurrentTradingSymbol(symbolParam);
    }
    if (actionParam && (actionParam === 'buy' || actionParam === 'sell')) {
      setOrderFormData(prev => ({
        ...prev,
        orderType: actionParam,
        priceType: actionParam === 'sell' && prev.priceType === 'stop' ? prev.priceType : 'market'
      }));
    }
  }, [symbolParam, actionParam]);

  // ================================
  // RENDER HELPERS
  // ================================

  /**
   * Render customer info section
   */
  const renderCustomerInfo = () => (
    <div className={styles.customerInfo}>
      <div className={styles.searchBox}>
        <input
          type="text"
          placeholder={t('trading.interface.customerSearch')}
          value={customerSearchQuery}
          onChange={(e) => setCustomerSearchQuery(e.target.value)}
          className={styles.searchInput}
        />
      </div>
      <div className={styles.customerDetails}>
        <span className={styles.customerName}>
          {t('trading.interface.customerName', { name: dashboardData?.userInfo?.name || "XXXXXX" })}
        </span>
        <span className={styles.accountNo}>
          {t('trading.interface.accountNumber', { number: "123456789" })}
        </span>
      </div>
      <div className={styles.balanceInfo}>
        <span className={styles.tutarText}>
          {t('trading.interface.totalBalance', { 
            balance: dashboardData?.totalBalance ? formatPriceToTurkishLira(dashboardData.totalBalance) : "₺0" 
          })}
        </span>
        <span className={styles.tutarAmount}>
          {t('trading.interface.totalCustomers', { count: dashboardData?.totalCustomers || 0 })}
        </span>
      </div>
    </div>
  );

  /**
   * Render trading form
   */
  const renderTradingForm = () => (
    <div className={styles.tradingFormPanel}>
      {/* Emir Ver Header */}
      <div className={styles.formHeader}>
        <span className={styles.formTitle}>{t('trading.orderForm.title')}</span>
        <span className={styles.orderTypeIndicator}>
          {orderFormData.orderType === "buy" ? t('trading.orderTypes.buyOrder') : t('trading.orderTypes.sellOrder')}
        </span>
      </div>

      <form onSubmit={handleOrderSubmission} className={styles.tradingForm}>

        {/* Hisse Seçimi */}
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>{t('trading.orderForm.stockSelectionRequired')}</label>
          <select 
            className={styles.stockSelect}
            value={currentTradingSymbol}
            onChange={(e) => handleSymbolChange(e.target.value)}
          >
            {availableSymbols.length > 0 ? (
              availableSymbols.map(stock => (
                <option key={stock.symbol} value={stock.symbol}>
                  {stock.symbol} - {stock.name}
                </option>
              ))
            ) : (
              <option value="">{t('trading.orderForm.stocksLoading')}</option>
            )}
          </select>
        </div>

        {/* İşlem Tipi Selection */}
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>{t('trading.orderForm.orderType')}</label>
          <div className={styles.radioGroup}>
            <label className={styles.radioOption}>
              <input
                type="radio"
                name="orderType"
                value="buy"
                checked={orderFormData.orderType === "buy"}
                onChange={() => setOrderFormData(prev => ({ 
                  ...prev, 
                  orderType: "buy",
                  priceType: prev.priceType === "stop" ? "market" : prev.priceType
                }))}
              />
              <span className={styles.radioText}>{t('trading.orderTypes.buy')}</span>
            </label>
            <label className={styles.radioOption}>
              <input
                type="radio"
                name="orderType"
                value="sell"
                checked={orderFormData.orderType === "sell"}
                onChange={() => setOrderFormData(prev => ({ ...prev, orderType: "sell" }))}
              />
              <span className={styles.radioText}>{t('trading.orderTypes.sell')}</span>
            </label>
          </div>
        </div>

        {/* Fiyat Tipi Selection */}
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>{t('trading.orderForm.priceType')}</label>
          <div className={styles.radioGroup}>
            <label className={styles.radioOption}>
              <input
                type="radio"
                name="priceType"
                value="market"
                checked={orderFormData.priceType === "market"}
                onChange={(e) => setOrderFormData(prev => ({ 
                  ...prev, 
                  priceType: e.target.value,
                  // Clear scheduled order when switching to market
                  isScheduled: e.target.value === "market" ? false : prev.isScheduled,
                  scheduledTime: e.target.value === "market" ? undefined : prev.scheduledTime
                }))}
              />
              <span className={styles.radioText}>{t('trading.priceTypes.market')}</span>
            </label>
            <label className={styles.radioOption}>
              <input
                type="radio"
                name="priceType"
                value="limit"
                checked={orderFormData.priceType === "limit"}
                onChange={(e) => setOrderFormData(prev => ({ ...prev, priceType: e.target.value }))}
              />
              <span className={styles.radioText}>{t('trading.priceTypes.limit')}</span>
            </label>
            {orderFormData.orderType === "sell" && (
              <label className={styles.radioOption}>
                <input
                  type="radio"
                  name="priceType"
                  value="stop"
                  checked={orderFormData.priceType === "stop"}
                  onChange={(e) => setOrderFormData(prev => ({ ...prev, priceType: e.target.value }))}
                />
                <span className={styles.radioText}>{t('trading.priceTypes.stop')}</span>
              </label>
            )}
          </div>
        </div>

        {/* Adet Input */}
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>{t('trading.orderForm.quantity')}</label>
          <input
            type="text"
            className={styles.standardInput}
            value={orderFormData.quantity}
            onChange={(e) => handleQuantityChange(e.target.value)}
            placeholder="0"
          />
        </div>

        {/* Limit Fiyat Input - Only show for limit/stop orders */}
        {orderFormData.priceType !== "market" && (
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>
              {orderFormData.priceType === "limit" ? t('trading.orderForm.limitPrice') : t('trading.orderForm.stopPrice')}
            </label>
            <input
              type="text"
              className={styles.standardInput}
              value={orderFormData.limitPrice}
              onChange={(e) => handleLimitPriceChange(e.target.value)}
              placeholder="0.00"
            />
          </div>
        )}

        {/* Güncel Fiyat Display */}
        <div className={styles.currentPriceDisplay}>
          <span className={styles.currentPriceLabel}>{t('trading.orderForm.currentPrice')} </span>
          <span className={styles.currentPriceValue}>
            {currentStockPrice ? formatPriceToTurkishLira(currentStockPrice.price) : "₺180,30"}
          </span>
        </div>


        {/* Scheduled Order Section - Enhanced UI - Only for non-market orders */}
        {orderFormData.priceType !== "market" && (
          <ScheduledOrderSection
            isScheduled={orderFormData.isScheduled}
            scheduledTime={orderFormData.scheduledTime}
            onScheduledChange={(isScheduled) => setOrderFormData(prev => ({ ...prev, isScheduled }))}
            onTimeChange={(time) => setOrderFormData(prev => ({ ...prev, scheduledTime: time }))}
            validation={scheduledOrderValidation}
            disabled={isSubmitting}
          />
        )}

        {/* Submit Button */}
        <button
          type="submit"
          disabled={isSubmitting || !isFormValid}
          className={`${styles.submitButton} ${orderFormData.orderType === "buy" ? styles.buyButton : styles.sellButton} ${!isFormValid ? styles.disabledButton : ""}`}
        >
          {isSubmitting 
            ? t('trading.messages.submitting')
            : orderFormData.orderType === "buy" ? t('trading.orderTypes.buyButton') : t('trading.orderTypes.sellButton')
          }
        </button>
      </form>
    </div>
  );

  // ================================
  // MAIN RENDER
  // ================================

  return (
    <div className={styles.container}>
        {/* Header Section */}
        <div className={styles.header}>
          <h1 className={styles.title}>{t('trading.title')}</h1>
          {renderCustomerInfo()}
        </div>

        {/* Trading Interface */}
        <div className={styles.tradingInterface}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <p className={styles.interfaceDesc}>{t('trading.interface.description')}</p>
            <ConnectionStatus
              marketDataStatus={marketDataStream.isConnected ? WebSocketStatus.CONNECTED : WebSocketStatus.DISCONNECTED}
              orderBookStatus={orderBookStream.isConnected ? WebSocketStatus.CONNECTED : WebSocketStatus.DISCONNECTED}
              tradeStreamStatus={tradeStream.isConnected ? WebSocketStatus.CONNECTED : WebSocketStatus.DISCONNECTED}
              showDetails={false}
            />
          </div>
          
          <div className={styles.mainContent}>
            {/* Left Panel - Real-time Order Book */}
            <div className={styles.orderBookPanel}>
              <PriceDisplay
                stockData={currentStockPrice}
                symbol={currentTradingSymbol}
                showChange={true}
                showVolume={false}
                isLive={marketDataStream.isConnected}
              />

              <OrderBookTable
                buyOrders={orderBookStream.buyOrders}
                sellOrders={orderBookStream.sellOrders}
                symbol={currentTradingSymbol}
                maxLevels={20}
                showSpread={true}
              />
            </div>

            {/* Right Panel - Trading Form */}
            {renderTradingForm()}
          </div>
        </div>
      </div>
  );
}