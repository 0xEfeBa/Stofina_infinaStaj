/**
 * WebSocket configuration management
 * Single Responsibility: Centralized WebSocket URL and settings management
 */

export interface WebSocketConnectionSettings {
  reconnectEnabled: boolean;
  reconnectDelay: number;
  maxReconnectAttempts: number;
  connectionTimeout: number;
}

export interface WebSocketUrls {
  MARKET_DATA_URL: string;
  ORDER_BOOK_URL: string;
  TRADE_EVENTS_URL: string;
}

/**
 * WebSocket configuration with environment variable support
 */
export const WEBSOCKET_CONFIG = {
  // WebSocket URLs with fallbacks
  URLS: {
    MARKET_DATA_URL: process.env.NEXT_PUBLIC_MARKET_DATA_WS_URL || 'ws://localhost:8081/ws/market-data',
    ORDER_BOOK_URL: process.env.NEXT_PUBLIC_ORDER_SERVICE_WS_URL || 'ws://localhost:8082/ws/orderbook',
    TRADE_EVENTS_URL: process.env.NEXT_PUBLIC_ORDER_SERVICE_WS_URL || 'ws://localhost:8082/ws/trades'
  } as WebSocketUrls,

  // Connection settings
  CONNECTION_SETTINGS: {
    reconnectEnabled: true,
    reconnectDelay: 2000,
    maxReconnectAttempts: 10,
    connectionTimeout: 10000
  } as WebSocketConnectionSettings,

  // Market Data specific settings
  MARKET_DATA: {
    updateFrequency: 1000, // 1 second
    defaultSymbols: ['THYAO', 'GARAN', 'ISCTR', 'AKBNK', 'TUPRS']
  },

  // Order Book specific settings
  ORDER_BOOK: {
    updateFrequency: 500, // 500ms
    maxLevels: 20 // Show top 20 levels
  },

  // Trade Events specific settings
  TRADE_EVENTS: {
    maxHistoryCount: 50 // Keep last 50 trades
  }
};

/**
 * Get WebSocket URL for Market Data Service
 */
export const getMarketDataWebSocketUrl = (): string => {
  return WEBSOCKET_CONFIG.URLS.MARKET_DATA_URL;
};

/**
 * Get WebSocket URL for Order Book streaming
 */
export const getOrderBookWebSocketUrl = (symbol: string): string => {
  const baseUrl = WEBSOCKET_CONFIG.URLS.ORDER_BOOK_URL;
  return `${baseUrl}/${symbol.toUpperCase()}`;
};

/**
 * Get WebSocket URL for Trade Events streaming
 */
export const getTradeEventsWebSocketUrl = (symbol: string): string => {
  const baseUrl = WEBSOCKET_CONFIG.URLS.TRADE_EVENTS_URL;
  return `${baseUrl}/${symbol.toUpperCase()}`;
};

/**
 * Get connection settings for WebSocket hooks
 */
export const getConnectionSettings = (): WebSocketConnectionSettings => {
  return { ...WEBSOCKET_CONFIG.CONNECTION_SETTINGS };
};

/**
 * Validate WebSocket URL format
 */
export const isValidWebSocketUrl = (url: string): boolean => {
  try {
    const urlObj = new URL(url);
    return urlObj.protocol === 'ws:' || urlObj.protocol === 'wss:';
  } catch {
    return false;
  }
};

/**
 * Get environment-specific WebSocket protocol
 */
export const getWebSocketProtocol = (): 'ws:' | 'wss:' => {
  // Use secure WebSocket in production
  return process.env.NODE_ENV === 'production' ? 'wss:' : 'ws:';
};

/**
 * Build WebSocket URL with proper protocol
 */
export const buildWebSocketUrl = (host: string, path: string): string => {
  const protocol = getWebSocketProtocol();
  const cleanHost = host.replace(/^(ws|wss):\/\//, '');
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  
  return `${protocol}//${cleanHost}${cleanPath}`;
};