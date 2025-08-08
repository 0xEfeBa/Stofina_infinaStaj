/**
 * Order submission hook
 * Single Responsibility: Handle order submission logic and state management
 */

"use client";

import { useCallback, useState } from 'react';
import { OrderFormData, OrderRequest, ApiResponse, OrderResponse } from '../types/order.types';
import { orderServiceAPI, createOrderRequest, formatOrderError } from '../services/orderService';

interface UseOrderSubmissionReturn {
  isSubmitting: boolean;
  submitOrder: (orderData: OrderFormData) => Promise<ApiResponse<OrderResponse>>;
  lastSubmissionError: string | null;
  clearError: () => void;
}

export const useOrderSubmission = (): UseOrderSubmissionReturn => {
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [lastSubmissionError, setLastSubmissionError] = useState<string | null>(null);

  const clearError = useCallback(() => {
    setLastSubmissionError(null);
  }, []);

  const submitOrder = useCallback(async (orderData: OrderFormData): Promise<ApiResponse<OrderResponse>> => {
    setIsSubmitting(true);
    setLastSubmissionError(null);

    try {
      // If orderType is directly provided, use it; otherwise create from side+type
      const orderRequest: OrderRequest = orderData.orderType ? {
        symbol: orderData.symbol.toUpperCase(),
        orderType: orderData.orderType,
        quantity: orderData.quantity,
        price: orderData.price,
        stopPrice: orderData.stopPrice,
        accountId: '1', // Default demo account ID
        tenantId: 1, // Default demo tenant ID
        isScheduled: orderData.isScheduled || false,
        scheduledTime: orderData.scheduledTime
      } : createOrderRequest(
        orderData.symbol,
        orderData.side,
        orderData.type!,
        orderData.quantity,
        orderData.price
      );

      const validation = await orderServiceAPI.validateOrder(orderRequest);
      
      if (!validation.success) {
        const errorMessage = formatOrderError(validation.error!);
        setLastSubmissionError(errorMessage);
        return validation as ApiResponse<OrderResponse>;
      }

      if (validation.data && !validation.data.valid) {
        const errorMessage = validation.data.message || 'Emir doğrulanamadı';
        setLastSubmissionError(errorMessage);
        return {
          success: false,
          error: {
            code: 'VALIDATION_FAILED',
            message: errorMessage
          }
        };
      }

      const result = await orderServiceAPI.submitOrder(orderRequest);

      if (!result.success && result.error) {
        const errorMessage = formatOrderError(result.error);
        setLastSubmissionError(errorMessage);
      }

      return result;

    } catch (error) {
      const errorMessage = 'Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.';
      setLastSubmissionError(errorMessage);
      
      return {
        success: false,
        error: {
          code: 'UNEXPECTED_ERROR',
          message: errorMessage,
          details: error
        }
      };
    } finally {
      setIsSubmitting(false);
    }
  }, []);

  return {
    isSubmitting,
    submitOrder,
    lastSubmissionError,
    clearError
  };
};