import { WebSocketError } from '../types/websocket.types';

/**
 * WebSocket utility constants
 */
export const WEBSOCKET_CONSTANTS = {
  DEFAULT_RECONNECT_DELAY: 3000,
  MAX_RECONNECT_ATTEMPTS: 5,
  CONNECTION_TIMEOUT: 10000,
  HEARTBEAT_INTERVAL: 30000,
  MESSAGE_QUEUE_SIZE: 100
} as const;

/**
 * Validates WebSocket URL format
 */
export const validateWebSocketUrl = (url: string): boolean => {
  if (!url || typeof url !== 'string') {
    return false;
  }
  
  try {
    const urlObj = new URL(url);
    return urlObj.protocol === 'ws:' || urlObj.protocol === 'wss:';
  } catch {
    return false;
  }
};

/**
 * Creates standardized WebSocket error
 */
export const createWebSocketError = (
  type: WebSocketError['type'],
  message: string,
  originalError?: Error
): WebSocketError => ({
  type,
  message,
  timestamp: new Date(),
  originalError
});

/**
 * Safely parses WebSocket message data
 */
export const parseWebSocketMessage = <T = any>(data: string): T | null => {
  try {
    return JSON.parse(data) as T;
  } catch (error) {
    console.error('Failed to parse WebSocket message:', error);
    return null;
  }
};

/**
 * Safely stringifies data for WebSocket sending
 */
export const stringifyWebSocketMessage = (data: any): string => {
  try {
    return JSON.stringify(data);
  } catch (error) {
    console.error('Failed to stringify WebSocket message:', error);
    throw new Error('Invalid message format for WebSocket');
  }
};

/**
 * Calculates exponential backoff delay
 */
export const calculateReconnectDelay = (
  attempt: number,
  baseDelay: number = WEBSOCKET_CONSTANTS.DEFAULT_RECONNECT_DELAY
): number => {
  return Math.min(baseDelay * Math.pow(2, attempt), 30000); // Max 30 seconds
};

/**
 * Checks if WebSocket is in a ready state
 */
export const isWebSocketReady = (socket: WebSocket | null): boolean => {
  return socket !== null && socket.readyState === WebSocket.OPEN;
};

/**
 * Gets human-readable WebSocket state
 */
export const getWebSocketStateText = (readyState: number): string => {
  switch (readyState) {
    case WebSocket.CONNECTING:
      return 'Connecting';
    case WebSocket.OPEN:
      return 'Connected';
    case WebSocket.CLOSING:
      return 'Closing';
    case WebSocket.CLOSED:
      return 'Disconnected';
    default:
      return 'Unknown';
  }
};