"use client";

import { useCallback, useEffect, useState } from 'react';
import { IMessage } from '@stomp/stompjs';
import { useSTOMPWebSocket } from './useSTOMPWebSocket';
import { StockPrice } from '../../types/marketData.types';
import { WEBSOCKET_CONFIG } from '../../config/websocket.config';

/**
 * Market data streaming hook using STOMP protocol
 * Compatible with Spring Boot WebSocket/STOMP backend
 */
export const useMarketDataStreamSTOMP = () => {
  // State for market data
  const [stockPrices, setStockPrices] = useState<Map<string, StockPrice>>(new Map());
  const [subscribedSymbols, setSubscribedSymbols] = useState<Set<string>>(new Set());
  const [lastUpdate, setLastUpdate] = useState<Date | null>(null);

  // STOMP WebSocket connection
  const { 
    isConnected, 
    isConnecting, 
    error, 
    subscribe, 
    send 
  } = useSTOMPWebSocket({
    url: 'http://localhost:8081/ws', // SockJS endpoint
    reconnectEnabled: true,
    reconnectDelay: 2000,
    maxReconnectAttempts: 10,
    debug: false
  });

  /**
   * Handle incoming market data messages
   */
  const handleMarketDataMessage = useCallback((message: IMessage) => {
    try {
      const data = JSON.parse(message.body);

      // Handle different message types from backend
      if (data.type === 'PRICE_UPDATE') {
        // Direct price update from backend
        setStockPrices(prev => {
          const newMap = new Map(prev);
          newMap.set(data.symbol.toUpperCase(), {
            symbol: data.symbol.toUpperCase(),
            price: data.price,
            change: data.changeAmount,
            changePercent: data.changePercent,
            volume: data.volume || 0,
            lastUpdated: new Date(data.timestamp),
            companyName: data.companyName || data.symbol
          });
          return newMap;
        });
        setLastUpdate(new Date());
      } else if (data.type === 'MARKET_DATA_UPDATE') {
        const update = data.payload;
        
        switch (update.type) {
          case 'PRICE_UPDATE':
            if (update.symbol) {
              const priceData = update.data;
              setStockPrices(prev => {
                const newMap = new Map(prev);
                newMap.set(update.symbol.toUpperCase(), {
                  symbol: priceData.symbol.toUpperCase(),
                  price: priceData.price,
                  change: priceData.change,
                  changePercent: priceData.changePercent,
                  volume: priceData.volume || 0,
                  lastUpdated: new Date(),
                  companyName: priceData.companyName || priceData.symbol
                });
                return newMap;
              });
              setLastUpdate(new Date());
            }
            break;
            
          case 'SYMBOL_LIST':
            // Handle bulk price updates
            const priceList = update.data;
            if (Array.isArray(priceList)) {
              setStockPrices(prev => {
                const newMap = new Map(prev);
                priceList.forEach((price: any) => {
                  newMap.set(price.symbol.toUpperCase(), {
                    symbol: price.symbol.toUpperCase(),
                    price: price.price,
                    change: price.change,
                    changePercent: price.changePercent,
                    volume: price.volume || 0,
                    lastUpdated: new Date(),
                    companyName: price.companyName || price.symbol
                  });
                });
                return newMap;
              });
              setLastUpdate(new Date());
            }
            break;
        }
      }
    } catch (err) {
      console.error('[Market Data] Failed to process message:', err);
    }
  }, []);

  /**
   * Subscribe to market data topic when connected
   */
  useEffect(() => {
    if (isConnected) {
      console.log('[Market Data] Subscribing to /topic/market-data');
      const unsubscribe = subscribe('/topic/market-data', handleMarketDataMessage);
      
      return unsubscribe;
    }
  }, [isConnected, subscribe, handleMarketDataMessage]);

  /**
   * Send subscription request when connected
   */
  useEffect(() => {
    if (isConnected) {
      const defaultSymbols = ['THYAO', 'GARAN', 'ISCTR', 'AKBNK', 'TUPRS', 'MGROS', 'TCELL', 'TUPRS', 'BRSAN', 'CCOLA'];
      
      const subscriptionMessage = {
        type: 'SUBSCRIBE',
        payload: {
          symbols: defaultSymbols,
          updateFrequency: 1000,
          includeVolume: true,
          includeExtendedHours: false
        },
        timestamp: new Date().toISOString(),
        id: `sub-${Date.now()}`
      };

      console.log('[Market Data] Sending subscription request:', subscriptionMessage);
      const success = send('/app/subscribe', subscriptionMessage);
      
      if (success) {
        setSubscribedSymbols(new Set(defaultSymbols.map(s => s.toUpperCase())));
      }
    }
  }, [isConnected, send]);

  /**
   * Subscribe to specific stock symbols
   */
  const subscribeToSymbols = useCallback((symbols: string[]) => {
    if (!isConnected || symbols.length === 0) {
      return false;
    }

    const subscriptionMessage = {
      type: 'SUBSCRIBE',
      payload: {
        symbols,
        updateFrequency: 1000,
        includeVolume: true,
        includeExtendedHours: false
      },
      timestamp: new Date().toISOString(),
      id: `sub-${Date.now()}`
    };

    const success = send('/app/subscribe', subscriptionMessage);
    if (success) {
      setSubscribedSymbols(prev => {
        const newSet = new Set(prev);
        symbols.forEach(symbol => newSet.add(symbol.toUpperCase()));
        return newSet;
      });
    }

    return success;
  }, [isConnected, send]);

  /**
   * Unsubscribe from specific stock symbols
   */
  const unsubscribeFromSymbols = useCallback((symbols: string[]) => {
    if (!isConnected || symbols.length === 0) {
      return false;
    }

    const unsubscribeMessage = {
      type: 'UNSUBSCRIBE',
      payload: symbols.map(s => s.toUpperCase()),
      timestamp: new Date().toISOString(),
      id: `unsub-${Date.now()}`
    };

    const success = send('/app/unsubscribe', unsubscribeMessage);
    if (success) {
      setSubscribedSymbols(prev => {
        const newSet = new Set(prev);
        symbols.forEach(symbol => newSet.delete(symbol.toUpperCase()));
        return newSet;
      });

      // Remove prices for unsubscribed symbols
      setStockPrices(prev => {
        const newMap = new Map(prev);
        symbols.forEach(symbol => newMap.delete(symbol.toUpperCase()));
        return newMap;
      });
    }

    return success;
  }, [isConnected, send]);

  /**
   * Get current price for a specific symbol
   */
  const getStockPrice = useCallback((symbol: string): StockPrice | null => {
    return stockPrices.get(symbol.toUpperCase()) || null;
  }, [stockPrices]);

  /**
   * Get all current stock prices
   */
  const getAllStockPrices = useCallback((): StockPrice[] => {
    return Array.from(stockPrices.values());
  }, [stockPrices]);

  /**
   * Check if symbol is subscribed
   */
  const isSymbolSubscribed = useCallback((symbol: string): boolean => {
    return subscribedSymbols.has(symbol.toUpperCase());
  }, [subscribedSymbols]);

  return {
    // Connection state
    isConnected,
    isConnecting,
    error,
    lastUpdate,
    
    // Data
    stockPrices: stockPrices,
    subscribedSymbols: Array.from(subscribedSymbols),
    
    // Methods
    subscribeToSymbols,
    unsubscribeFromSymbols,
    getStockPrice,
    getAllStockPrices,
    isSymbolSubscribed
  };
};