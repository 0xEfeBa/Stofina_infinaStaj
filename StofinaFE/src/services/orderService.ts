/**
 * Order Service API Integration
 * Single Responsibility: Handle all order-related API communications
 */

import { OrderRequest, OrderResponse, ApiResponse, ApiError } from '../types/order.types';

class OrderServiceAPI {
  private readonly baseUrl: string;
  private readonly defaultHeaders: HeadersInit;

  constructor() {
    this.baseUrl = process.env.NEXT_PUBLIC_ORDER_SERVICE_URL || 'http://localhost:8082';
    this.defaultHeaders = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };
  }

  private getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('accessToken');
    return {
      ...this.defaultHeaders,
      ...(token && { 'Authorization': `Bearer ${token}` })
    };
  }

  private async handleResponse<T>(response: Response): Promise<ApiResponse<T>> {
    try {
      const data = await response.json();

      if (!response.ok) {
        return {
          success: false,
          error: {
            code: response.status.toString(),
            message: data.message || `HTTP ${response.status}: ${response.statusText}`,
            details: data
          }
        };
      }

      return {
        success: true,
        data
      };
    } catch (error) {
      return {
        success: false,
        error: {
          code: 'PARSE_ERROR',
          message: 'Failed to parse server response',
          details: error
        }
      };
    }
  }

  async submitOrder(orderRequest: OrderRequest): Promise<ApiResponse<OrderResponse>> {
    try {
      const response = await fetch(`${this.baseUrl}/api/v1/orders`, {
        method: 'POST',
        headers: this.getAuthHeaders(),
        body: JSON.stringify(orderRequest)
      });

      return this.handleResponse<OrderResponse>(response);
    } catch (error) {
      return {
        success: false,
        error: {
          code: 'NETWORK_ERROR',
          message: 'Failed to connect to Order Service',
          details: error
        }
      };
    }
  }

  async validateOrder(orderRequest: OrderRequest): Promise<ApiResponse<{ valid: boolean; message?: string }>> {
    try {
      const response = await fetch(`${this.baseUrl}/api/v1/orders/validate`, {
        method: 'POST',
        headers: this.getAuthHeaders(),
        body: JSON.stringify(orderRequest)
      });

      return this.handleResponse<{ valid: boolean; message?: string }>(response);
    } catch (error) {
      return {
        success: false,
        error: {
          code: 'NETWORK_ERROR',
          message: 'Failed to validate order',
          details: error
        }
      };
    }
  }

  async getUserOrders(accountId?: string): Promise<ApiResponse<OrderResponse[]>> {
    try {
      const params = new URLSearchParams();
      if (accountId) params.append('accountId', accountId);

      const response = await fetch(`${this.baseUrl}/api/v1/orders?${params}`, {
        method: 'GET',
        headers: this.getAuthHeaders()
      });

      return this.handleResponse<OrderResponse[]>(response);
    } catch (error) {
      return {
        success: false,
        error: {
          code: 'NETWORK_ERROR',
          message: 'Failed to fetch orders',
          details: error
        }
      };
    }
  }

  async cancelOrder(orderId: string): Promise<ApiResponse<{ cancelled: boolean }>> {
    try {
      const response = await fetch(`${this.baseUrl}/api/v1/orders/${orderId}`, {
        method: 'DELETE',
        headers: this.getAuthHeaders()
      });

      return this.handleResponse<{ cancelled: boolean }>(response);
    } catch (error) {
      return {
        success: false,
        error: {
          code: 'NETWORK_ERROR',
          message: 'Failed to cancel order',
          details: error
        }
      };
    }
  }
}

export const orderServiceAPI = new OrderServiceAPI();

/**
 * Map frontend side+type to backend orderType enum
 */
const mapToOrderType = (side: 'BUY' | 'SELL', type: 'MARKET' | 'LIMIT' | 'STOP'): string => {
  if (side === 'BUY') {
    switch (type) {
      case 'MARKET': return 'MARKET_BUY';
      case 'LIMIT': return 'LIMIT_BUY';
      case 'STOP': return 'STOP_LOSS_SELL'; // Stop orders are always sell
    }
  } else { // SELL
    switch (type) {
      case 'MARKET': return 'MARKET_SELL';
      case 'LIMIT': return 'LIMIT_SELL';
      case 'STOP': return 'STOP_LOSS_SELL';
    }
  }
  throw new Error(`Invalid combination: ${side} ${type}`);
};

export const createOrderRequest = (
  symbol: string,
  side: 'BUY' | 'SELL',
  type: 'MARKET' | 'LIMIT' | 'STOP',
  quantity: number,
  price?: number,
  accountId?: string
): OrderRequest => {
  const orderType = mapToOrderType(side, type);
  
  return {
    symbol: symbol.toUpperCase(),
    orderType,
    quantity,
    ...(price && { price }),
    accountId: accountId || '1', // Default demo account ID
    tenantId: 1 // Default demo tenant ID
  };
};

export const formatOrderError = (error: ApiError): string => {
  switch (error.code) {
    case '400':
      return 'Geçersiz emir verisi. Lütfen formu kontrol edin.';
    case '401':
      return 'Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.';
    case '403':
      return 'Bu işlem için yetkiniz yok.';
    case '422':
      return 'Emir işlenemiyor: ' + (error.details?.message || error.message);
    case 'INSUFFICIENT_BALANCE':
      return 'Yetersiz bakiye. Lütfen hesap bakiyenizi kontrol edin.';
    case 'MARKET_CLOSED':
      return 'Piyasa kapalı. İşlem saatleri dışında emir veremezsiniz.';
    case 'INVALID_SYMBOL':
      return 'Geçersiz sembol. Lütfen farklı bir hisse seçin.';
    case 'NETWORK_ERROR':
      return 'Bağlantı hatası. Lütfen internet bağlantınızı kontrol edin.';
    default:
      return error.message || 'Bilinmeyen bir hata oluştu.';
  }
};