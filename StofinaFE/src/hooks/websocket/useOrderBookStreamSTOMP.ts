"use client";

import { useCallback, useEffect, useState } from 'react';
import { IMessage } from '@stomp/stompjs';
import { useSTOMPWebSocket } from './useSTOMPWebSocket';
import { OrderBookLevel } from '../../types/orderbook.types';

/**
 * Order book streaming hook using STOMP protocol
 * Compatible with Spring Boot WebSocket/STOMP backend
 */
export const useOrderBookStreamSTOMP = (symbol: string) => {
  // State for order book data
  const [buyOrders, setBuyOrders] = useState<OrderBookLevel[]>([]);
  const [sellOrders, setSellOrders] = useState<OrderBookLevel[]>([]);
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
    url: 'http://localhost:8082/ws/orderbook', // SockJS endpoint
    reconnectEnabled: true,
    reconnectDelay: 2000,
    maxReconnectAttempts: 10,
    debug: false
  });

  /**
   * Handle incoming order book messages
   */
  const handleOrderBookMessage = useCallback((message: IMessage) => {
    console.log('[Order Book] ⭐ MESAJ GELDİ:', message.body);
    try {
      const data = JSON.parse(message.body);
      console.log('[Order Book] Parsed data:', data);

      // Handle order book updates
      if (data.type === 'ORDER_BOOK_UPDATE') {
        const orderBookData = data.payload;
        
        if (orderBookData.symbol === currentSymbol.toUpperCase()) {
          const bookData = orderBookData.data;
          setBuyOrders(bookData.bids || []);
          setSellOrders(bookData.asks || []);
          setLastUpdate(new Date());
          console.log('[Order Book] Updated - Bids:', bookData.bids?.length, 'Asks:', bookData.asks?.length);
        }
      }
    } catch (err) {
      console.error('[Order Book] Failed to process message:', err);
    }
  }, [currentSymbol]);

  /**
   * Subscribe to order book topic when connected
   */
  useEffect(() => {
    if (isConnected && currentSymbol) {
      const topic = `/topic/orderbook/${currentSymbol.toUpperCase()}`;
      console.log('[Order Book] Subscribing to:', topic);
      const unsubscribe = subscribe(topic, handleOrderBookMessage);
      
      return unsubscribe;
    }
  }, [isConnected, currentSymbol, subscribe, handleOrderBookMessage]);

  /**
   * Send subscription request when connected or symbol changes
   */
  useEffect(() => {
    if (isConnected && currentSymbol) {
      const subscriptionMessage = {
        type: 'SUBSCRIBE_ORDER_BOOK',
        payload: {
          symbol: currentSymbol.toUpperCase(),
          maxLevels: 20
        },
        timestamp: new Date().toISOString(),
        id: `orderbook-sub-${Date.now()}`
      };

      console.log('[Order Book] Sending subscription request:', subscriptionMessage);
      send(`/app/orderbook/subscribe/${currentSymbol}`, subscriptionMessage);
    }
  }, [isConnected, currentSymbol, send]);

  /**
   * Update symbol when prop changes
   */
  useEffect(() => {
    if (symbol !== currentSymbol) {
      setCurrentSymbol(symbol);
      // Clear existing data when symbol changes
      setBuyOrders([]);
      setSellOrders([]);
      setLastUpdate(null);
    }
  }, [symbol, currentSymbol]);

  /**
   * Get best bid price
   */
  const getBestBid = useCallback((): number | null => {
    return buyOrders.length > 0 ? buyOrders[0].price : null;
  }, [buyOrders]);

  /**
   * Get best ask price
   */
  const getBestAsk = useCallback((): number | null => {
    return sellOrders.length > 0 ? sellOrders[0].price : null;
  }, [sellOrders]);

  /**
   * Get bid-ask spread
   */
  const getSpread = useCallback((): number | null => {
    const bid = getBestBid();
    const ask = getBestAsk();
    return bid && ask ? ask - bid : null;
  }, [getBestBid, getBestAsk]);

  /**
   * Get total volume at specific price level
   */
  const getVolumeAtPrice = useCallback((price: number, side: 'buy' | 'sell'): number => {
    const orders = side === 'buy' ? buyOrders : sellOrders;
    const level = orders.find(order => order.price === price);
    return level ? level.quantity : 0;
  }, [buyOrders, sellOrders]);

  return {
    // Connection state
    isConnected,
    isConnecting,
    error,
    lastUpdate,
    
    // Data
    buyOrders,
    sellOrders,
    symbol: currentSymbol,
    
    // Calculated values
    bestBid: getBestBid(),
    bestAsk: getBestAsk(),
    spread: getSpread(),
    
    // Methods
    getBestBid,
    getBestAsk,
    getSpread,
    getVolumeAtPrice
  };
};