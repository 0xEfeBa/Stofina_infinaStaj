"use client";

import React, { useState, useMemo, useCallback, useEffect } from "react";
import { useRouter, useSearchParams } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import styles from "./Trading.module.css";
import { useDashboardContext } from "../../../../contexts/DashboardContext";

// Import WebSocket hooks
import { useMarketDataStreamSTOMP } from "@/hooks/websocket/useMarketDataStreamSTOMP";
import { useOrderBookStreamSTOMP } from "@/hooks/websocket/useOrderBookStreamSTOMP";
import { useTradeStreamSTOMP } from "@/hooks/websocket/useTradeStreamSTOMP";

// Import components
import { PriceDisplay } from "./components/PriceDisplay";
import { OrderBookTable } from "./components/OrderBookTable";
import { ConnectionStatus } from "./components/ConnectionStatus";

// Import types
import { WebSocketStatus } from "@/types/websocket.types";

// Import utilities
import {
  calculateOrderTotalValue,
  InvalidQuantityError,
  InvalidPriceError
} from "@/utils/tradingCalculations";
import { formatPriceToTurkishLira } from "@/utils/priceFormatters";

// Import order submission hook
import { useOrderSubmission } from "@/hooks/useOrderSubmission";

// Import market data API hook
import { useMarketDataAPI } from "@/hooks/useMarketDataAPI";

// Import types
import { OrderFormData } from "@/types/order.types";

// Import scheduled order components and hooks
import { ScheduledOrderSection } from "@/components/ScheduledOrderSection";
import { useScheduledOrder } from "@/hooks/useScheduledOrder";
import { formatScheduledTimeForAPI, validateScheduledOrderForm } from "@/utils/scheduledOrderUtils";
import AutoCompleteCustomerSearch from "@/components/common/AutoCompleteCustomerSearch";
import AccountSelector from "@/components/order-tracking/accountSelector";
import { Account } from "@/types/account";
import { useSelectorCustom } from "@/store";
import { useDispatchCustom } from "@/hooks/useDispatchCustom";
import { thunkAccount } from "@/thunks/accountThunk";
import { SliceGlobalModal } from "@/slice/common/sliceGlobalModal";

interface InternalOrderFormData {
  orderType: string;
  priceType: string;
  limitPrice: string;
  quantity: string;
  isScheduled: boolean;
  scheduledTime?: Date;
}


export default function TradingPage() {


  // i18n hook for translations
  const { t } = useTranslation();
  const router = useRouter();
  const dispatch = useDispatchCustom();
  const [openAccountSelector, setOpenAccountSelector] = useState(false);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);
  const { selectedIndividualCustomer, selectedCorporateCustomer } = useSelectorCustom(state => state.customer);


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


  // Dashboard context for user info
  const { dashboardData } = useDashboardContext();

  // Market data stream (prices) - Using STOMP protocol
  const marketDataStream = useMarketDataStreamSTOMP();

  // Order book stream (buy/sell orders) - Using STOMP protocol
  const orderBookStream = useOrderBookStreamSTOMP(currentTradingSymbol);

  // Trade stream (executed trades) - Using STOMP protocol
  const tradeStream = useTradeStreamSTOMP(currentTradingSymbol);

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
    if (!selectedAccount) {
      return { isValid: false, errorMessage: t('trading.messages.accountRequired') };
    }
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

    // WebSocket bağlantı kontrolü - Market order için
    if (orderFormData.priceType === "market" && !marketDataStream.isConnected) {
      return { isValid: false, errorMessage: "Market emri için canlı veri bağlantısı gereklidir" };
    }

    // Market fiyat yaşı kontrolü (30 saniye)
    if (orderFormData.priceType === "market" && currentStockPrice) {
      const priceAge = new Date().getTime() - currentStockPrice.lastUpdated.getTime();
      if (priceAge > 30000) { // 30 saniye
        return { isValid: false, errorMessage: "Fiyat verisi güncel değil. Lütfen birkaç saniye bekleyin" };
      }
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

  useEffect(() => {
    fetchAccounts();
  }, [selectedIndividualCustomer, selectedCorporateCustomer]);

  const fetchAccounts = async () => {
    if (selectedIndividualCustomer) {
      const response = await dispatch(thunkAccount.getAccountsByCustomerId(selectedIndividualCustomer?.customer.id));
      if (response) {
        setAccounts(response);
        setOpenAccountSelector(true);
      }
    }
    else if (selectedCorporateCustomer) {
      const response = await dispatch(thunkAccount.getAccountsByCustomerId(selectedCorporateCustomer?.customer.id));
      if (response) {
        setAccounts(response);
        setOpenAccountSelector(true);
      }
    }
    else {
      setAccounts([]);
      setOpenAccountSelector(false);
    }
  }

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
        accountId: selectedAccount?.id?.toString() || "",
        symbol: currentTradingSymbol,
        orderType: backendOrderType as any, // We'll need to update the type definition
        side: orderFormData.orderType.toUpperCase() as 'BUY' | 'SELL',
        quantity: parseFloat(orderFormData.quantity),
        price: orderFormData.priceType === "market"
          ? currentStockPrice?.price  // Market emirler için anlık fiyat
          : orderFormData.priceType === "limit"
            ? parseFloat(orderFormData.limitPrice)  // Limit emirler için kullanıcı fiyatı
            : undefined,
        stopPrice: orderFormData.priceType === "stop" ? parseFloat(orderFormData.limitPrice) : undefined,
      };

      // Submit order to backend
      const result = await submitOrder(orderData);

      if (result.success) {
        // Show success message
        const orderTypeText = orderFormData.orderType === "buy" ? t('trading.orderTypes.buy') : t('trading.orderTypes.sell');
        // alert(`${t('trading.messages.success', { type: orderTypeText })}\n${t('trading.messages.orderId', { id: result.data?.orderId })}`);

        dispatch(SliceGlobalModal.actions.openModal({
          modalType: "success",
          message: `${t('trading.messages.success', { type: orderTypeText })}\n${t('trading.messages.orderId', { id: result.data?.orderId })}`,
          multipleButtons: false,

        }))

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
        // alert(lastSubmissionError || t('trading.messages.unexpectedError'));
        dispatch(SliceGlobalModal.actions.openModal({
          modalType: "error",
          message: lastSubmissionError || t('trading.messages.unexpectedError'),
          multipleButtons: false,

        }))
      }

    } catch (error) {
      console.error('Order submission error:', error);
      // alert(t('trading.messages.unexpectedError'));
      dispatch(SliceGlobalModal.actions.openModal({
        modalType: "error",
        message: lastSubmissionError || t('trading.messages.unexpectedError'),
        multipleButtons: false,

      }))
    }
  }, [orderFormData, currentTradingSymbol, validateOrderForm, submitOrder, clearError, lastSubmissionError]);


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


  const renderCustomerInfo = () => (
    <div className={styles.customerInfo}>

      <div className={styles.customerDetails}>
        <div className="flex flex-col gap-2">
          <span className={styles.customerName}>
            {t('trading.interface.customerName', {
              name: selectedIndividualCustomer
                ? selectedIndividualCustomer.firstName + " " + selectedIndividualCustomer.lastName
                : selectedCorporateCustomer
                  ? selectedCorporateCustomer.tradeName
                  : ""
            })}
          </span>
          <span className={styles.accountNo}>
            {t('trading.interface.accountNumber', {
              number: selectedIndividualCustomer?.id || selectedCorporateCustomer?.id
            })}
          </span>
          <span className={styles.tutarText}>
            {t('trading.interface.totalBalance', {
              balance: selectedAccount?.availableBalance ? formatPriceToTurkishLira(selectedAccount.availableBalance) : ""
            })}
          </span>
        </div>

      </div>
      <div className={styles.balanceInfo}>

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
          {/* Market order uyarısı */}
          {orderFormData.priceType === "market" && (
            <div className={styles.marketOrderNote}>
              <small style={{ color: '#666', fontSize: '12px' }}>
                {marketDataStream.isConnected
                  ? "Bu emir anlık piyasa fiyatından işleme alınacaktır"
                  : "Canlı veri bağlantısı yok - Market emri verilemez"
                }
              </small>
            </div>
          )}
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
      <div className="flex  gap-4">
        <div className=' flex items-start mb-14'>
          <button type="button" className={styles.secondaryButton} onClick={() => router.back()}>
            <img src="/menu-icon/back.png" alt={t('report.back')} className={styles.icon} />
            {t('common.back')}
          </button>
          <AccountSelector open={openAccountSelector} onClose={() => setOpenAccountSelector(false)} accounts={accounts} onSelect={setSelectedAccount} />
          <AutoCompleteCustomerSearch />
        </div>
        <div className={styles.header}>
          {renderCustomerInfo()}
        </div>

      </div>


      {/* Trading Interface */}
      <div className={styles.tradingInterface}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          {/* <p className={styles.interfaceDesc}>{t('trading.interface.description')}</p> */}
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