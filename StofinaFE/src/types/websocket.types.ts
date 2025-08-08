/**
 * WebSocket connection states
 */
export enum WebSocketStatus {
  DISCONNECTED = 'disconnected',
  CONNECTING = 'connecting',
  CONNECTED = 'connected',
  RECONNECTING = 'reconnecting',
  ERROR = 'error'
}

/**
 * WebSocket configuration options
 */
export interface WebSocketConfig {
  url: string;
  protocols?: string | string[];
  reconnectEnabled?: boolean;
  reconnectDelay?: number;
  maxReconnectAttempts?: number;
  connectionTimeout?: number;
}

/**
 * WebSocket error types
 */
export interface WebSocketError {
  type: 'connection' | 'message' | 'timeout' | 'protocol';
  message: string;
  timestamp: Date;
  originalError?: Error;
}

/**
 * WebSocket hook return interface
 */
export interface UseWebSocketReturn {
  socket: WebSocket | null;
  status: WebSocketStatus;
  error: WebSocketError | null;
  isConnected: boolean;
  isConnecting: boolean;
  connect: () => void;
  disconnect: () => void;
  send: (data: any) => boolean;
  lastMessage: any;
}

/**
 * WebSocket message wrapper
 */
export interface WebSocketMessage<T = any> {
  type: string;
  payload: T;
  timestamp: string;
  id?: string;
}