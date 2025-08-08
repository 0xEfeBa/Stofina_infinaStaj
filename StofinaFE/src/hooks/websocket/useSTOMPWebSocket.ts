"use client";

import { useCallback, useEffect, useRef, useState } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface STOMPWebSocketConfig {
  url: string;
  reconnectEnabled?: boolean;
  reconnectDelay?: number;
  maxReconnectAttempts?: number;
  debug?: boolean;
}

export interface UseSTOMPWebSocketReturn {
  isConnected: boolean;
  isConnecting: boolean;
  error: string | null;
  client: Client | null;
  subscribe: (destination: string, callback: (message: IMessage) => void) => () => void;
  send: (destination: string, body: any) => boolean;
  disconnect: () => void;
}

/**
 * STOMP WebSocket hook for Spring Boot WebSocket integration
 * Handles connection management, subscriptions, and message sending
 */
export const useSTOMPWebSocket = (config: STOMPWebSocketConfig): UseSTOMPWebSocketReturn => {
  // State
  const [isConnected, setIsConnected] = useState<boolean>(false);
  const [isConnecting, setIsConnecting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  
  // Refs
  const clientRef = useRef<Client | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const reconnectAttemptRef = useRef<number>(0);
  const subscriptionsRef = useRef<Map<string, () => void>>(new Map());

  const {
    url,
    reconnectEnabled = true,
    reconnectDelay = 2000,
    maxReconnectAttempts = 10,
    debug = false
  } = config;

  /**
   * Connect to STOMP WebSocket
   */
  const connect = useCallback(() => {
    if (clientRef.current?.connected) {
      return;
    }

    setIsConnecting(true);
    setError(null);

    try {
      // Create SockJS instance
      const socket = new SockJS(url);
      
      // Create STOMP client
      const client = new Client({
        webSocketFactory: () => socket,
        debug: debug ? (str: string) => console.log('[STOMP Debug]', str) : () => {},
        reconnectDelay: reconnectEnabled ? reconnectDelay : 0,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      // Connection handlers
      client.onConnect = () => {
        console.log('[STOMP] Connected to', url);
        setIsConnected(true);
        setIsConnecting(false);
        setError(null);
        reconnectAttemptRef.current = 0;
      };

      client.onDisconnect = () => {
        console.log('[STOMP] Disconnected from', url);
        setIsConnected(false);
        setIsConnecting(false);
        
        // Clear subscriptions
        subscriptionsRef.current.clear();
        
        // Attempt reconnect if enabled
        if (reconnectEnabled && reconnectAttemptRef.current < maxReconnectAttempts) {
          reconnectAttemptRef.current++;
          console.log(`[STOMP] Reconnecting... Attempt ${reconnectAttemptRef.current}/${maxReconnectAttempts}`);
          
          reconnectTimeoutRef.current = setTimeout(() => {
            connect();
          }, reconnectDelay);
        }
      };

      client.onStompError = (frame) => {
        const errorMessage = `STOMP error: ${frame.headers['message'] || 'Unknown error'}`;
        console.error('[STOMP Error]', errorMessage, frame.body);
        setError(errorMessage);
        setIsConnecting(false);
      };

      client.onWebSocketError = (event) => {
        const errorMessage = `WebSocket error: ${event.type}`;
        console.error('[STOMP WebSocket Error]', errorMessage, event);
        setError(errorMessage);
        setIsConnecting(false);
      };

      clientRef.current = client;
      client.activate();

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Connection failed';
      setError(errorMessage);
      setIsConnecting(false);
      console.error('[STOMP] Connection error:', err);
    }
  }, [url, reconnectEnabled, reconnectDelay, maxReconnectAttempts, debug]);

  /**
   * Disconnect from STOMP WebSocket
   */
  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    if (clientRef.current) {
      clientRef.current.deactivate();
      clientRef.current = null;
    }

    subscriptionsRef.current.clear();
    setIsConnected(false);
    setIsConnecting(false);
    setError(null);
  }, []);

  /**
   * Subscribe to a destination
   */
  const subscribe = useCallback((destination: string, callback: (message: IMessage) => void) => {
    if (!clientRef.current?.connected) {
      console.warn('[STOMP] Cannot subscribe - not connected');
      return () => {};
    }

    console.log('[STOMP] Subscribing to:', destination);
    
    const subscription = clientRef.current.subscribe(destination, callback);
    
    const unsubscribe = () => {
      subscription.unsubscribe();
      subscriptionsRef.current.delete(destination);
      console.log('[STOMP] Unsubscribed from:', destination);
    };

    subscriptionsRef.current.set(destination, unsubscribe);
    return unsubscribe;
  }, []);

  /**
   * Send message to destination
   */
  const send = useCallback((destination: string, body: any) => {
    if (!clientRef.current?.connected) {
      console.warn('[STOMP] Cannot send - not connected');
      return false;
    }

    try {
      const messageBody = typeof body === 'string' ? body : JSON.stringify(body);
      clientRef.current.publish({
        destination,
        body: messageBody,
        headers: {
          'content-type': 'application/json'
        }
      });
      
      console.log('[STOMP] Message sent to:', destination);
      return true;
    } catch (err) {
      console.error('[STOMP] Send error:', err);
      return false;
    }
  }, []);

  // Initialize connection on mount
  useEffect(() => {
    connect();
    
    // Cleanup on unmount
    return () => {
      disconnect();
    };
  }, [connect, disconnect]);

  return {
    isConnected,
    isConnecting,
    error,
    client: clientRef.current,
    subscribe,
    send,
    disconnect
  };
};