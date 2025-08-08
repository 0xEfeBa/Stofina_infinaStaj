// CLEAN CODE: Custom hook for scheduled order business logic
// Single Responsibility: Handle all scheduled order state and actions

import { useState, useCallback, useMemo } from 'react';
import { 
  ScheduledOrderForm, 
  ScheduledOrderValidation, 
  CreateScheduledOrderRequest,
  ScheduledOrderResponse,
  OrderType,
  OrderSide 
} from '../types/scheduledOrder.types';
import { 
  validateScheduledOrderForm, 
  formatScheduledTimeForAPI, 
  generateClientOrderId,
  getNextValidMarketTime 
} from '../utils/scheduledOrderUtils';

// Initial form state
const createInitialFormState = (): ScheduledOrderForm => ({
  accountId: 1, // TODO: Get from user context
  tenantId: 1,  // TODO: Get from user context
  symbol: '',
  orderType: 'LIMIT_BUY',
  side: 'BUY',
  quantity: 0,
  price: undefined,
  stopPrice: undefined,
  timeInForce: 'DAY',
  clientOrderId: generateClientOrderId('WEB'),
  isBot: false,
  isScheduled: false,
  scheduledTime: undefined
});

interface UseScheduledOrderReturn {
  readonly formData: ScheduledOrderForm;
  readonly validation: ScheduledOrderValidation;
  readonly isSubmitting: boolean;
  readonly error: string | null;
  readonly updateFormData: <K extends keyof ScheduledOrderForm>(
    field: K, 
    value: ScheduledOrderForm[K]
  ) => void;
  readonly updateMultipleFields: (updates: Partial<ScheduledOrderForm>) => void;
  readonly setScheduledTime: (date: Date | undefined) => void;
  readonly toggleScheduled: () => void;
  readonly resetForm: () => void;
  readonly submitOrder: () => Promise<ScheduledOrderResponse | null>;
  readonly canSubmit: boolean;
}

export const useScheduledOrder = (): UseScheduledOrderReturn => {
  // State management
  const [formData, setFormData] = useState<ScheduledOrderForm>(createInitialFormState);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // PURE FUNCTION: Validation is computed from current form state
  const validation = useMemo((): ScheduledOrderValidation => {
    return validateScheduledOrderForm(
      formData.isScheduled,
      formData.scheduledTime,
      formData.symbol,
      formData.quantity
    );
  }, [formData.isScheduled, formData.scheduledTime, formData.symbol, formData.quantity]);
  
  // PURE FUNCTION: Can submit when valid and not submitting
  const canSubmit = useMemo((): boolean => {
    return validation.isValid && !isSubmitting && formData.symbol !== '' && formData.quantity > 0;
  }, [validation.isValid, isSubmitting, formData.symbol, formData.quantity]);
  
  // CLEAN CODE: Single field update function
  const updateFormData = useCallback(<K extends keyof ScheduledOrderForm>(
    field: K, 
    value: ScheduledOrderForm[K]
  ): void => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    setError(null); // Clear error on form change
  }, []);
  
  // CLEAN CODE: Multiple fields update function
  const updateMultipleFields = useCallback((updates: Partial<ScheduledOrderForm>): void => {
    setFormData(prev => ({
      ...prev,
      ...updates
    }));
    setError(null);
  }, []);
  
  // CLEAN CODE: Scheduled time specific setter with smart defaults
  const setScheduledTime = useCallback((date: Date | undefined): void => {
    setFormData(prev => ({
      ...prev,
      scheduledTime: date
    }));
    setError(null);
  }, []);
  
  // CLEAN CODE: Toggle scheduled with smart default time
  const toggleScheduled = useCallback((): void => {
    setFormData(prev => ({
      ...prev,
      isScheduled: !prev.isScheduled,
      scheduledTime: !prev.isScheduled && !prev.scheduledTime 
        ? getNextValidMarketTime() 
        : prev.scheduledTime
    }));
    setError(null);
  }, []);
  
  // CLEAN CODE: Reset form to initial state
  const resetForm = useCallback((): void => {
    setFormData(createInitialFormState());
    setError(null);
  }, []);
  
  // CLEAN CODE: Submit order with proper error handling
  const submitOrder = useCallback(async (): Promise<ScheduledOrderResponse | null> => {
    if (!canSubmit) {
      setError('Form geçerli değil, lütfen kontrol edin');
      return null;
    }
    
    setIsSubmitting(true);
    setError(null);
    
    try {
      // Transform form data to API request format
      const apiRequest: CreateScheduledOrderRequest = {
        accountId: formData.accountId,
        tenantId: formData.tenantId,
        symbol: formData.symbol,
        orderType: formData.orderType,
        side: formData.side,
        quantity: formData.quantity,
        price: formData.price,
        stopPrice: formData.stopPrice,
        timeInForce: formData.timeInForce,
        clientOrderId: formData.clientOrderId,
        isBot: formData.isBot,
        isScheduled: formData.isScheduled,
        scheduledTime: formData.scheduledTime 
          ? formatScheduledTimeForAPI(formData.scheduledTime) 
          : undefined
      };
      
      // Make API call
      const response = await fetch('http://localhost:8082/api/v1/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(apiRequest)
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Emir oluşturulamadı: ${errorText}`);
      }
      
      const result: ScheduledOrderResponse = await response.json();
      
      // Reset form on success
      resetForm();
      
      return result;
      
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Bilinmeyen hata oluştu';
      setError(errorMessage);
      return null;
    } finally {
      setIsSubmitting(false);
    }
  }, [canSubmit, formData, resetForm]);
  
  return {
    formData,
    validation,
    isSubmitting,
    error,
    updateFormData,
    updateMultipleFields,
    setScheduledTime,
    toggleScheduled,
    resetForm,
    submitOrder,
    canSubmit
  };
};