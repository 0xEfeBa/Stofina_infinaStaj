"use client";

import { useCallback, useEffect, useState } from 'react';
import { IMessage } from '@stomp/stompjs';
import { useSTOMPWebSocket } from './useSTOMPWebSocket';
import { TradeEvent } from '../../types/trading.types';

/**
 * Trade stream hook using STOMP protocol
 * Compatible with Spring Boot WebSocket/STOMP backend
 */
export const useTradeStreamSTOMP = (symbol: string) => {
  // State for trade data
  const [recentTrades, setRecentTrades] = useState<TradeEvent[]>([]);
  const [lastTrade, setLastTrade] = useState<TradeEvent | null>(null);
  const [lastUpdate, setLastUpdate] = useState<Date | null>(null);
  const [currentSymbol, setCurrentSymbol] = useState<string>(symbol);

  // STOMP WebSocket connection
  const { 
    isConnected, 
    isConnecting, 
    error, 
    subscribe, 
    send 
  } = useSTOMPWebSocket({
    url: 'http://localhost:9006/ws/trades', // SockJS endpoint
    reconnectEnabled: true,
    reconnectDelay: 2000,
    maxReconnectAttempts: 10,
    debug: false
  });

  /**
   * Handle incoming trade messages
   */
  const handleTradeMessage = useCallback((message: IMessage) => {
    try {
      const data = JSON.parse(message.body);
      console.log('[Trade Stream] Received:', data);

      // Handle trade events
      if (data.type === 'TRADE_EXECUTED') {
        const tradeData = data.payload as TradeEvent;
        
        if (tradeData.symbol === currentSymbol.toUpperCase()) {
          // Add to recent trades (keep last 50)
          setRecentTrades(prev => {
            const newTrades = [tradeData, ...prev].slice(0, 50);
            return newTrades;
          });
          
          setLastTrade(tradeData);
          setLastUpdate(new Date());
        }
      }
    } catch (err) {
      console.error('[Trade Stream] Failed to process message:', err);
    }
  }, [currentSymbol]);

  /**
   * Subscribe to trade topic when connected
   */
  useEffect(() => {
    if (isConnected && currentSymbol) {
      const topic = `/topic/trades/${currentSymbol.toUpperCase()}`;
      console.log('[Trade Stream] Subscribing to:', topic);
      const unsubscribe = subscribe(topic, handleTradeMessage);
      
      return unsubscribe;
    }
  }, [isConnected, currentSymbol, subscribe, handleTradeMessage]);

  /**
   * Send subscription request when connected or symbol changes
   */
  useEffect(() => {
    if (isConnected && currentSymbol) {
      const subscriptionMessage = {
        type: 'SUBSCRIBE_TRADES',
        payload: {
          symbol: currentSymbol.toUpperCase(),
          maxHistory: 50
        },
        timestamp: new Date().toISOString(),
        id: `trades-sub-${Date.now()}`
      };

      console.log('[Trade Stream] Sending subscription request:', subscriptionMessage);
      send('/app/trades/subscribe', subscriptionMessage);
    }
  }, [isConnected, currentSymbol, send]);

  /**
   * Update symbol when prop changes
   */
  useEffect(() => {
    if (symbol !== currentSymbol) {
      setCurrentSymbol(symbol);
      // Clear existing data when symbol changes
      setRecentTrades([]);
      setLastTrade(null);
      setLastUpdate(null);
    }
  }, [symbol, currentSymbol]);

  /**
   * Get last trade price
   */
  const getLastTradePrice = useCallback((): number | null => {
    return lastTrade ? lastTrade.trade?.price || null : null;
  }, [lastTrade]);

  /**
   * Get trade volume in last N minutes
   */
  const getVolumeInTimeRange = useCallback((minutes: number): number => {
    const cutoffTime = new Date(Date.now() - minutes * 60 * 1000);
    return recentTrades
      .filter(trade => new Date(trade.timestamp) > cutoffTime)
      .reduce((total, trade) => total + (trade.trade?.quantity || 0), 0);
  }, [recentTrades]);

  /**
   * Get total volume for the session
   */
  const getTotalVolume = useCallback((): number => {
    return recentTrades.reduce((total, trade) => total + (trade.trade?.quantity || 0), 0);
  }, [recentTrades]);

  /**
   * Get average trade price
   */
  const getAveragePrice = useCallback((): number | null => {
    if (recentTrades.length === 0) return null;
    
    const validTrades = recentTrades.filter(trade => trade.trade);
    const totalValue = validTrades.reduce((sum, trade) => sum + (trade.trade!.price * trade.trade!.quantity), 0);
    const totalQuantity = getTotalVolume();
    
    return totalQuantity > 0 ? totalValue / totalQuantity : null;
  }, [recentTrades, getTotalVolume]);

  return {
    // Connection state
    isConnected,
    isConnecting,
    error,
    lastUpdate,
    
    // Data
    recentTrades,
    lastTrade,
    symbol: currentSymbol,
    
    // Calculated values
    lastTradePrice: getLastTradePrice(),
    totalVolume: getTotalVolume(),
    averagePrice: getAveragePrice(),
    
    // Methods
    getLastTradePrice,
    getVolumeInTimeRange,
    getTotalVolume,
    getAveragePrice
  };
};