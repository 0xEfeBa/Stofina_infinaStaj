/**
 * Order-related type definitions
 */

export interface OrderRequest {
  symbol: string;
  orderType: string; // Backend expects orderType (MARKET_BUY, LIMIT_SELL, etc.)
  quantity: number;
  price?: number;
  stopPrice?: number; // For stop-loss orders
  accountId: string;
  tenantId: number;
  isScheduled?: boolean; // For scheduled orders
  scheduledTime?: string; // For scheduled orders (ISO format)
}

export interface OrderResponse {
  orderId: string;
  symbol: string;
  side: 'BUY' | 'SELL';
  type: 'MARKET' | 'LIMIT' | 'STOP';
  quantity: number;
  price?: number;
  status: 'PENDING' | 'FILLED' | 'PARTIALLY_FILLED' | 'CANCELLED' | 'REJECTED';
  timestamp: string;
  accountId: string;
  tenantId: string;
}

export interface OrderFormData {
  symbol: string;
  orderType?: string; // MARKET_BUY, LIMIT_SELL, STOP_LOSS_SELL etc.
  side: 'BUY' | 'SELL';
  type?: 'MARKET' | 'LIMIT' | 'STOP'; // Optional for backward compatibility
  quantity: number;
  price?: number;
  stopPrice?: number; // For stop-loss orders
  isScheduled: boolean;
  scheduledTime?: string; // ISO format string for API ("2025-08-04 15:30:00")
}

export interface ApiError {
  code: string;
  message: string;
  details?: any;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: ApiError;
}

export interface OrderValidation {
  isValid: boolean;
  errorMessage?: string;
}